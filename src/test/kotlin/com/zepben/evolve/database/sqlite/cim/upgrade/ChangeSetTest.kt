/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade

import com.zepben.evolve.database.paths.DatabaseType
import com.zepben.evolve.database.sqlite.cim.tables.tableCimVersion
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.ChangeSetValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.combined.*
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.customer.ChangeSet50CustomerValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.customer.ChangeSet51CustomerValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.customer.ChangeSet52CustomerValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.diagram.ChangeSet50DiagramValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.diagram.ChangeSet51DiagramValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.diagram.ChangeSet52DiagramValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.network.ChangeSet50NetworkValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.network.ChangeSet51NetworkValidator
import com.zepben.evolve.database.sqlite.cim.upgrade.changesets.network.ChangeSet52NetworkValidator
import com.zepben.testutils.junit.SystemLogExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.slf4j.LoggerFactory
import org.sqlite.SQLiteException
import java.io.File
import java.sql.Connection
import java.sql.DriverManager.getConnection

@Suppress("SqlResolve", "SameParameterValue")
internal class ChangeSetTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val logger = LoggerFactory.getLogger(javaClass)

    private val preSplitChangeSetValidators = mapOf(
        44 to ChangeSet44Validator,
        45 to ChangeSet45Validator,
        46 to ChangeSet46Validator,
        47 to ChangeSet47Validator,
        48 to ChangeSet48Validator,
        49 to ChangeSet49Validator
    )

    private val customerChangeSetValidators = mapOf(
        50 to ChangeSet50CustomerValidator,
        51 to ChangeSet51CustomerValidator,
        52 to ChangeSet52CustomerValidator
    )

    private val diagramChangeSetValidators = mapOf(
        50 to ChangeSet50DiagramValidator,
        51 to ChangeSet51DiagramValidator,
        52 to ChangeSet52DiagramValidator
    )

    private val networkChangeSetValidators = mapOf(
        50 to ChangeSet50NetworkValidator,
        51 to ChangeSet51NetworkValidator,
        52 to ChangeSet52NetworkValidator
    )

    @Test
    internal fun `test change sets`() {
        // All pre-split change sets are for the network database, as that is the only database that existed.
        validateChangeSets(createBaseCombinedDB(), preSplitChangeSetValidators, UpgradeRunner::preSplitChangeSets, DatabaseType.NETWORK_MODEL)

        validateChangeSets(createBaseDB(DatabaseType.CUSTOMER), customerChangeSetValidators, UpgradeRunner::postSplitChangeSets, DatabaseType.CUSTOMER)
        validateChangeSets(createBaseDB(DatabaseType.DIAGRAM), diagramChangeSetValidators, UpgradeRunner::postSplitChangeSets, DatabaseType.DIAGRAM)
        validateChangeSets(createBaseDB(DatabaseType.NETWORK_MODEL), networkChangeSetValidators, UpgradeRunner::postSplitChangeSets, DatabaseType.NETWORK_MODEL)
    }

    /**
     * Creates an in memory sqlite database using the base combined schema (from version 43).
     */
    private fun createBaseCombinedDB(): Connection =
        createDatabaseFromScript("src/test/data/base-schema.sql")

    /**
     * Creates an in memory sqlite database using the base network schema (from version 49).
     */
    private fun createBaseDB(type: DatabaseType): Connection =
        createDatabaseFromScript("src/test/data/base-${type.fileDescriptor}-schema.sql")

    private fun createDatabaseFromScript(script: String): Connection =
        getConnection("jdbc:sqlite::memory:").apply {
            createStatement().use { statement ->
                File(script).readLines().forEach {
                    statement.executeUpdate(it)
                }
            }
        }

    private fun validateChangeSets(
        conn: Connection,
        changeSetValidators: Map<Int, ChangeSetValidator>,
        changesSets: UpgradeRunner.() -> List<ChangeSet>,
        type: DatabaseType
    ) {
        val runner = UpgradeRunner()
        val tableVersion = tableCimVersion

        conn.createStatement().use { stmt ->
            conn.prepareStatement(tableVersion.preparedUpdateSql).use { versionUpdateStatement ->
                runner.changesSets().forEach { cs ->

                    val validator = changeSetValidators[cs.number]
                        ?: throw IllegalStateException("Validator for change set ${cs.number} is missing. Have you added a ChangeSetValidator for your latest model update?")

                    logger.info("Preparing for update ${cs.number}.")
                    validator.setUpStatements().forEach {
                        stmt.executeUpdate(it)
                    }

                    stmt.executeUpdate("BEGIN TRANSACTION")
                    stmt.executeUpdate("PRAGMA foreign_keys=ON")
                    runner.runUpgrade(cs, stmt, versionUpdateStatement, type)

                    logger.info("Populating after update ${cs.number}.")
                    validator.populateStatements().forEach {
                        try {
                            stmt.executeUpdate(it)
                        } catch (e: SQLiteException) {
                            throw SQLiteException("Failed executing update error was: ${e.message}\n Query was: $it: ", e.resultCode)
                        }
                    }
                    stmt.executeUpdate("PRAGMA foreign_key_check")
                    stmt.executeUpdate("COMMIT")

                    logger.info("Validating update ${cs.number}.")
                    validator.let { csv ->
                        csv.validate(stmt)
                        csv.tearDownStatements().forEach {
                            stmt.executeUpdate(it)
                        }
                    }
                }
            }
        }
    }

}
