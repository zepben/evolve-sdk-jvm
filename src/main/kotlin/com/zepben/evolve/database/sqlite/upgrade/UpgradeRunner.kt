/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.upgrade

import com.zepben.evolve.database.sqlite.extensions.configureBatch
import com.zepben.evolve.database.sqlite.extensions.executeConfiguredQuery
import com.zepben.evolve.database.sqlite.tables.TableVersion
import com.zepben.evolve.database.sqlite.upgrade.changesets.*
import com.zepben.evolve.services.common.extensions.asUnmodifiable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.*
import java.sql.*

@Suppress("SqlResolve")
class UpgradeRunner constructor(
    private val getConnection: (String) -> Connection = DriverManager::getConnection,
    private val getStatement: (Connection) -> Statement = Connection::createStatement,
    private val getPreparedStatement: (Connection, String) -> PreparedStatement = Connection::prepareStatement,
    private val createBackup: (databaseFilename: Path, backupFilename: Path, copyOption: CopyOption) -> Unit = { f, b, o -> Files.copy(f, b, o) },
    internal val changeSets: List<ChangeSet> =
        listOf(
            changeSet15(),
            changeSet16(),
            changeSet17(),
            changeSet18(),
            changeSet19(),
            changeSet20(),
            changeSet21(),
            changeSet22(),
            changeSet23(),
            changeSet24(),
            changeSet25(),
            changeSet26(),
            changeSet27(),
            changeSet28(),
            changeSet29(),
            changeSet30(),
            changeSet31(),
            changeSet32(),
            changeSet33(),
            changeSet34(),
            changeSet35(),
        ).asUnmodifiable()
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val tableVersion = TableVersion()

    init {
        changeSets.forEachIndexed { index, changeSet ->
            val previous: Int = changeSets.getOrNull(index - 1)?.number ?: (changeSet.number - 1)
            require(previous == changeSet.number - 1) {
                "Change set has been registered in non-sequential order. Did you bump the change set number correctly?"
            }
        }

        require(changeSets.last().number == tableVersion.SUPPORTED_VERSION) {
            if (changeSets.last().number > tableVersion.SUPPORTED_VERSION)
                "The last registered change set is newer than the supported version. Did you forget to bump the supported version number?"
            else
                "The last registered change set is older than the supported version. Did you forget to register a change set?"
        }
    }

    @Throws(SQLException::class)
    fun connectAndUpgrade(databaseDescriptor: String, databaseFile: Path): ConnectionResult {
        val connection = runCatching { getConnection(databaseDescriptor).configureBatch(getStatement) }
            .getOrElse { throw UpgradeException(it.message, it) }

        return try {
            getStatement(connection).use { statement ->
                upgrade(databaseFile, statement, connection)
            }

            ConnectionResult(connection, tableVersion.SUPPORTED_VERSION)
        } catch (e: Exception) {
            //
            // NOTE: Closing the connection will rollback any active transactions (see https://www.sqlite.org/lang_transaction.html).
            //
            connection.close()
            when (e) {
                is UpgradeException, is SQLException -> throw UpgradeException("Failed to execute upgrade scripts. ${e.message}", e)
                is IOException, is SecurityException -> throw UpgradeException("Failed to create database backup. ${e.message}", e)
                else -> throw e
            }
        }
    }

    @Throws(SQLException::class, IOException::class, SecurityException::class)
    private fun upgrade(databaseFile: Path, statement: Statement, connection: Connection) {
        var backupCreated = false
        fun doBackup(databaseVersion: Int) {
            if (backupCreated)
                return

            logger.info("Upgrading database '{}' from v{} to v{}", databaseFile, databaseVersion, changeSets.last().number)
            createBackup(databaseFile, createBackupName(databaseFile, databaseVersion), StandardCopyOption.REPLACE_EXISTING)
            backupCreated = true
        }

        val versionResult = getVersion(statement)
        val databaseVersion = when {
            versionResult.versionNumber != null -> versionResult.versionNumber
            versionResult.requiresVersionTableUpgrdae -> {
                val version = getVersionOldSchema(statement) ?: throw UpgradeException("No version number found in database.")
                doBackup(version)
                upgradeVersionTable(statement, version)
                version
            }
            else -> throw UpgradeException("No version number found in database.")
        }

        when {
            databaseVersion < 14 -> throw UpgradeException("Upgrading a database before v14 is unsupported. Please generate a new database from the source system.")
            databaseVersion > tableVersion.SUPPORTED_VERSION -> throw SQLException("Selected database is a newer version [v$databaseVersion] than the supported version [${tableVersion.SUPPORTED_VERSION}].")
            databaseVersion < tableVersion.SUPPORTED_VERSION -> {
                doBackup(databaseVersion)

                getPreparedStatement(connection, tableVersion.preparedUpdateSql()).use { versionUpdateStatement ->
                    changeSets.asSequence()
                        .filter { databaseVersion < it.number }
                        .forEach { runUpgrade(it, statement, versionUpdateStatement) }
                }
            }
        }
    }

    @Throws(SQLException::class, SecurityException::class)
    internal fun runUpgrade(
        changeSet: ChangeSet,
        statement: Statement,
        versionUpdateStatement: PreparedStatement
    ) {
        logger.info("Applying database change set ${changeSet.number}")
        statement.executeUpdate("PRAGMA foreign_keys=OFF")

        changeSet.preCommandsHook(statement)

        for (sql in changeSet.commands())
            statement.executeUpdate(sql)

        changeSet.postCommandsHook(statement)

        updateVersion(versionUpdateStatement, changeSet.number)

        statement.executeUpdate("PRAGMA foreign_key_check")
        statement.executeUpdate("COMMIT")
        statement.executeUpdate("BEGIN TRANSACTION")
        statement.executeUpdate("PRAGMA foreign_keys=ON")
    }

    private fun createBackupName(databaseFile: Path, changeSetNumber: Int): Path {
        val filename = databaseFile.toString()

        val pos = filename.lastIndexOf(".")
        return if (pos > 0)
            Paths.get(filename.substring(0, pos) + "-v" + changeSetNumber + filename.substring(pos))
        else
            Paths.get("$filename-v$changeSetNumber")
    }

    @Throws(SQLException::class)
    private fun updateVersion(versionUpdateStatement: PreparedStatement, version: Int) {
        versionUpdateStatement.setInt(tableVersion.VERSION.queryIndex, version)
        versionUpdateStatement.executeUpdate()
    }

    @Throws(SQLException::class)
    private fun getVersion(statement: Statement): VersionResult {
        return try {
            statement.executeConfiguredQuery(tableVersion.selectSql()).use { results ->
                if (results.next())
                    VersionResult(results.getInt(tableVersion.VERSION.queryIndex))
                else
                    VersionResult(null)
            }
        } catch (e: SQLException) {
            VersionResult(null, true)
        }
    }

    @Throws(SQLException::class)
    private fun getVersionOldSchema(statement: Statement): Int? {
        return statement.executeQuery("SELECT major FROM version").use { results ->
            if (results.next())
                results.getInt(1)
            else
                null
        }
    }

    @Throws(SQLException::class)
    private fun upgradeVersionTable(statement: Statement, version: Int) {
        statement.execute("DROP TABLE version")
        statement.execute(tableVersion.createTableSql())
        statement.executeUpdate("INSERT into ${tableVersion.VERSION.name} VALUES ($version)")
    }

    class ConnectionResult(val connection: Connection, val version: Int)
    class UpgradeException(message: String?, cause: Throwable? = null) : Exception(message, cause)

    private data class VersionResult(val versionNumber: Int?, val requiresVersionTableUpgrdae: Boolean = false)
}
