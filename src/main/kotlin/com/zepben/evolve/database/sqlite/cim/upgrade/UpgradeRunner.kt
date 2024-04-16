/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade

import com.zepben.evolve.database.paths.DatabaseType
import com.zepben.evolve.database.sqlite.extensions.configureBatch
import com.zepben.evolve.database.sqlite.common.TableVersion
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.*
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.changeSet46
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.changeSet47
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.changeSet49
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.changeSet50
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.changeSet51
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.*
import java.sql.*

/**
 * Upgrade the schema in one of our databases to the latest version.
 */
class UpgradeRunner @JvmOverloads constructor(
    private val getConnection: (String) -> Connection = DriverManager::getConnection,
    private val copyFile: (source: Path, target: Path, copyOption: CopyOption) -> Unit = { f, b, o -> Files.copy(f, b, o) },
    internal val preSplitChangeSets: List<ChangeSet> = listOf(
        changeSet44(),
        changeSet45(),
        changeSet46(),
        changeSet47(),
        changeSet48(),
        changeSet49()
    ),
    // Rename this to `changeSets` and remove database splitting logic next time we set a new minimum version of the database.
    internal val postSplitChangeSets: List<ChangeSet> = listOf(
        changeSet50(),
        changeSet51(),
        changeSet52()
    ),
    private val tableVersion: TableVersion = tableCimVersion
) {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)
    private val minimumSupportedVersion = preSplitChangeSets.firstOrNull()?.let { it.number - 1 } ?: tableVersion.supportedVersion

    // Remove this variable next time we set a new minimum version of the database. Also remove the note in NetworkDatabaseReader.
    private val splitVersion = 49

    /**
     * Get a connection to the database, and run any required upgrade scripts to bring the schema into line.
     *
     * NOTE: The caller of this function is responsible for closing the returned [Connection]
     *
     * @param databaseDescriptor The JDBC connection string used to establish the [Connection] to the database.
     * @param databaseFile The path to the database. Used to create backups and log progress.
     *
     * @return The [Connection] to the upgraded database.
     */
    @Throws(UpgradeException::class)
    fun connectAndUpgrade(databaseDescriptor: String, databaseFile: Path, type: DatabaseType): ConnectionResult {
        val connection = runCatching { getConnection(databaseDescriptor).configureBatch() }
            .getOrElse { throw UpgradeException(it.message, it) }

        return try {
            val preSplitStatus = upgrade(databaseFile, connection, type, preSplitChangeSets, true)

            // This will copy the database file into the new split format. Remove it next time we set a new minimum version of the database.
            // Unless we are already past the split, split the database. Take a backup only if we haven't done so in upgrading to the split.
            if (preSplitStatus != UpgradeState.AHEAD_OF_TARGET_VERSION)
                splitDatabase(databaseFile, preSplitStatus == UpgradeState.ALREADY_AT_TARGET_VERSION)

            // We only need to take a backup if it wasn't already done as part of the upgrade to the split database.
            val postSplitStatus = upgrade(databaseFile, connection, type, postSplitChangeSets, preSplitStatus == UpgradeState.AHEAD_OF_TARGET_VERSION)

            connection.vacuumDatabase(postSplitStatus == UpgradeState.UPGRADED_TO_TARGET_VERSION)

            ConnectionResult(connection, tableVersion.supportedVersion)
        } catch (e: Exception) {
            //
            // NOTE: Closing the connection will roll back any active transactions (see https://www.sqlite.org/lang_transaction.html).
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
    private fun upgrade(
        databaseFile: Path,
        connection: Connection,
        type: DatabaseType,
        changeSets: List<ChangeSet>,
        backupRequired: Boolean
    ): UpgradeState =
        connection.createStatement().use { statement ->
            tryRunUpgrade(tableVersion.getVersion(statement), changeSets.last().number) { databaseVersion ->
                logger.info("Upgrading database '$databaseFile' from v$databaseVersion to v${changeSets.last().number}")

                if (backupRequired)
                    copyFile(databaseFile, createBackupName(databaseFile, databaseVersion), StandardCopyOption.REPLACE_EXISTING)

                connection.prepareStatement(tableVersion.preparedUpdateSql).use { versionUpdateStatement ->
                    changeSets.asSequence()
                        .filter { databaseVersion < it.number }
                        .forEach { runUpgrade(it, statement, versionUpdateStatement, type) }
                }
            }
        }

    private fun tryRunUpgrade(databaseVersion: Int?, targetVersion: Int, upgradeBlock: (databaseVersion: Int) -> Unit): UpgradeState =
        when {
            databaseVersion == targetVersion -> UpgradeState.ALREADY_AT_TARGET_VERSION
            databaseVersion == null -> throwMissingVersionException()
            databaseVersion > tableVersion.supportedVersion -> throwFuturisticDatabaseException(databaseVersion)
            databaseVersion < minimumSupportedVersion -> throwUnsupportedUpgradeException(databaseVersion)
            databaseVersion > targetVersion -> UpgradeState.AHEAD_OF_TARGET_VERSION
            else -> upgradeBlock(databaseVersion).let { UpgradeState.UPGRADED_TO_TARGET_VERSION }
        }

    @Throws(SQLException::class, SecurityException::class)
    internal fun runUpgrade(
        changeSet: ChangeSet,
        statement: Statement,
        versionUpdateStatement: PreparedStatement,
        type: DatabaseType
    ) {
        logger.info("Applying database change set ${changeSet.number}")
        statement.executeUpdate("PRAGMA foreign_keys=OFF")

        changeSet.preCommandHooks.filter { type in it.targetDatabases }.forEach { it(statement) }

        changeSet.commands.filter { type in it.targetDatabases }.flatMap { it.commands }.forEach {
            statement.executeUpdate(it)
        }

        changeSet.postCommandHooks.filter { type in it.targetDatabases }.forEach { it(statement) }

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

    @Throws(SQLException::class, IOException::class, SecurityException::class)
    private fun splitDatabase(networkDatabaseFile: Path, requiresBackup: Boolean) {
        if (requiresBackup)
            copyFile(networkDatabaseFile, createBackupName(networkDatabaseFile, splitVersion), StandardCopyOption.REPLACE_EXISTING)

        cloneAndUpgrade(networkDatabaseFile, DatabaseType.CUSTOMER)
        cloneAndUpgrade(networkDatabaseFile, DatabaseType.DIAGRAM)
    }

    private fun cloneAndUpgrade(networkDatabaseFile: Path, targetType: DatabaseType) {
        val targetDatabaseFile = networkDatabaseFile.replace(DatabaseType.NETWORK_MODEL.fileDescriptor, targetType.fileDescriptor)

        try {
            copyFile(networkDatabaseFile, targetDatabaseFile, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: Exception) {
            throw UpgradeException("Failed to split database. ${e.message}", e)
        }

        runCatching { getConnection("jdbc:sqlite:$targetDatabaseFile").configureBatch() }
            .getOrElse { throw UpgradeException(it.message, it) }
            .use { connection ->
                upgrade(targetDatabaseFile, connection, targetType, postSplitChangeSets, false)
                connection.vacuumDatabase(true)
            }
    }

    private fun throwMissingVersionException(): Nothing =
        throw UpgradeException(
            "Invalid EWB database detected, unable to read the version number from the database. Please ensure you only have databases " +
                "in the EWB data directory that have been generated by a working migrator."
        )

    private fun throwFuturisticDatabaseException(databaseVersion: Int): Nothing =
        throw UpgradeException(
            "Selected database is a newer version [v$databaseVersion] than the supported version [v${tableVersion.supportedVersion}]. Either update the " +
                "version of the server you are using, or downgrade the migrator."
        )

    private fun throwUnsupportedUpgradeException(databaseVersion: Int): Nothing =
        throw UpgradeException(
            "Unable to upgrade obsolete database version [v$databaseVersion], upgrading a database before v$minimumSupportedVersion is unsupported. Please " +
                "generate a new database from the source system using an updated migrator."
        )

    private fun Path.replace(oldValue: String, newValue: String): Path =
        Path.of(toString().replace(oldValue, newValue))

    private fun Connection.vacuumDatabase(shouldVacuum: Boolean) {
        // Vacuum the database to reclaim unused space if requested.
        if (shouldVacuum)
            createStatement().use {
                it.executeUpdate("COMMIT")
                it.executeUpdate("VACUUM")
            }
    }

    /**
     * A connection to the database and its schema version number.
     *
     * @property connection The [Connection] to the database.
     * @property version The schema version number of the database.
     */
    class ConnectionResult(val connection: Connection, val version: Int)

    /**
     * An exception indicating the upgrade has failed.
     *
     * @param message A message indicating what error has occurred.
     * @param cause Any underlying cause of the error.
     */
    class UpgradeException(message: String?, cause: Throwable? = null) : Exception(message, cause)

    private enum class UpgradeState {
        ALREADY_AT_TARGET_VERSION,
        UPGRADED_TO_TARGET_VERSION,
        AHEAD_OF_TARGET_VERSION
    }

}
