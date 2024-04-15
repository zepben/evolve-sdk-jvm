/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.upgrade

import com.zepben.evolve.database.paths.DatabaseType
import com.zepben.evolve.database.sqlite.extensions.configureBatch
import com.zepben.evolve.database.sqlite.cim.tables.Column
import com.zepben.evolve.database.sqlite.cim.tables.TableVersion
import com.zepben.evolve.database.sqlite.cim.upgrade.Change
import com.zepben.evolve.database.sqlite.cim.upgrade.ChangeSet
import com.zepben.evolve.database.sqlite.cim.upgrade.EwbDatabaseType
import com.zepben.evolve.database.sqlite.cim.upgrade.UpgradeRunner
import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.mockk.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.io.IOException
import java.nio.file.*
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement

internal class UpgradeRunnerTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private var currentVersion = 0
    private var nextVersion = 0

    private val statement = mockk<Statement> {
        // For database control commands that we run such as "BEGIN TRANSACTION", "COMMIT" etc.
        every { executeUpdate(any()) } returns 1
        justRun { close() }
        justRun { queryTimeout = 30 }
        justRun { fetchSize = 10000 }
    }
    private val preparedStatement = mockk<PreparedStatement> {
        // For updating the version number.
        every { executeUpdate() } answers {
            currentVersion = nextVersion
            1
        }
        justRun { close() }
        every { setInt(1, any()) } answers { nextVersion = secondArg() }
    }
    private val connection = mockk<Connection> {
        justRun { autoCommit = any() }
        every { createStatement() } returns statement
        every { prepareStatement(any()) } returns preparedStatement

        //
        // NOTE: We use the connection close event to reset the version to the split version. We need to do this so the post split upgrades run on each
        //       database, and the connections are closed as part of this processing, giving us a nice signal to do so.
        //
        every { close() } answers { currentVersion = splitVersion }
    }
    private val connectionProvider = spyk<(String) -> Connection>({ connection })
    private val copyFile = spyk<(Path, Path, CopyOption) -> Unit>({ _, _, _ -> })

    // NOTE: Use these to avoid calling `.number` on a mockk ChangeSet.
    private val minSupportedVersion = 47
    private val splitVersion = 49
    private val maxSupportedVersion = 52

    // Pre-split changesets must be before (and including) splitVersion
    private val minChangeSet = changeSetOf(minSupportedVersion)
    private val preSplitChangeSet = changeSetOf(minSupportedVersion + 1)
    private val lastPreSplitChangeSet = changeSetOf(splitVersion)

    // Post-split must be above (excluding) splitVersion.
    private val firstPostSplitChangeSet = changeSetOf(splitVersion + 1)
    private val postSplitChangeSet = changeSetOf(maxSupportedVersion - 1)
    private val maxChangeSet = changeSetOf(maxSupportedVersion)

    private val preSplitChangeSets = listOf(minChangeSet, preSplitChangeSet, lastPreSplitChangeSet)
    private val postSplitChangeSets = listOf(firstPostSplitChangeSet, postSplitChangeSet, maxChangeSet)

    private val versionColumn = mockk<Column> { every { queryIndex } returns 1 }
    private val tableVersion = mockk<TableVersion> {
        every { SUPPORTED_VERSION } returns maxSupportedVersion
        every { VERSION } returns versionColumn
        every { selectSql } returns "select version"
        every { preparedUpdateSql } returns "update statement"

        // Don't use a `returns` here as we don't want to cache the value.
        every { getVersion(any()) } answers { currentVersion }
    }

    private val upgradeRunner = UpgradeRunner(connectionProvider, copyFile, preSplitChangeSets, postSplitChangeSets, tableVersion)

    @Disabled
    @Test
    internal fun `upgrade real customer file`() {
        // Put the name of the database you want to load in src/test/resources/test-customer-database.txt
        upgradeRealFile("test-customer-database.txt", DatabaseType.CUSTOMER)
    }

    @Disabled
    @Test
    internal fun `upgrade real diagram file`() {
        // Put the name of the database you want to load in src/test/resources/test-diagram-database.txt
        upgradeRealFile("test-diagram-database.txt", DatabaseType.DIAGRAM)
    }

    @Disabled
    @Test
    internal fun `upgrade real network file`() {
        // Put the name of the database you want to load in src/test/resources/test-network-database.txt
        upgradeRealFile("test-network-database.txt", DatabaseType.NETWORK_MODEL)
    }

    @Test
    internal fun `has changesets for each supported version in order`() {
        // Revert this test to the commented out version next time we set a new minimum version of the database.
//        val actualChangeSets = UpgradeRunner().changeSets
//
//        // If upgrades are supported, make sure they are all registered in the correct order.
//        if (actualChangeSets.isNotEmpty())
//            assertThat(actualChangeSets.map { it.number }, equalTo((actualChangeSets.minOf { it.number }..TableVersion().SUPPORTED_VERSION).toList()))
        UpgradeRunner().apply {
            val actualChangeSets = preSplitChangeSets + postSplitChangeSets

            assertThat("preSplitChangeSets must be numbered up to the split version.", preSplitChangeSets.last().number, equalTo(splitVersion))
            assertThat(preSplitChangeSets.maxOf { it.number }, equalTo(postSplitChangeSets.minOf { it.number } - 1))

            // If upgrades are supported, make sure they are all registered in the correct order.
            if (actualChangeSets.isNotEmpty())
                assertThat(actualChangeSets.map { it.number }, equalTo((actualChangeSets.minOf { it.number }..TableVersion().SUPPORTED_VERSION).toList()))
        }
    }

    @Test
    internal fun `configures connection for batch processing`() {
        mockkStatic("com.zepben.evolve.database.sqlite.extensions.ConnectionExtensionsKt") {
            currentVersion = maxSupportedVersion - 1

            upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)

            verify(exactly = 1) { connectionProvider("driver:database") }
            verify(exactly = 1) { connection.configureBatch() }
        }
    }

    @Test
    internal fun `runs each changeset in transactions`() {
        val networkFile = "something-${DatabaseType.NETWORK_MODEL.fileDescriptor}.sqlite"
        val databaseDescriptor = "driver:database:$networkFile"
        currentVersion = minSupportedVersion - 1

        val connectionResult = upgradeRunner.connectAndUpgrade(databaseDescriptor, Paths.get(networkFile), DatabaseType.NETWORK_MODEL)

        assertThat(connectionResult.connection, sameInstance(connection))
        assertThat(connectionResult.version, equalTo(maxSupportedVersion))

        verifySequence {
            // Checks the minimum supported changeset number.
            minChangeSet.number

            validateConnectionToDatabase(DatabaseType.NETWORK_MODEL)
            validatePreSplitChangesets()

            // Clones the database into the customer and diagram variants and runs the post-split commands on them.
            validateCloneAndUpgrade(DatabaseType.CUSTOMER)
            validateCloneAndUpgrade(DatabaseType.DIAGRAM)

            // Continues to run the post-split commands on the network database.
            validatePostSplitChangesets(DatabaseType.NETWORK_MODEL)
            validateVacuum()

            // Returns the upgraded version number
            tableVersion.SUPPORTED_VERSION
        }
    }

    @Test
    internal fun `doesn't run upgrade if current version`() {
        currentVersion = maxSupportedVersion

        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)

        // Should check the changeset number in the following cases:
        // 1. of the minimum changeset to determine the minimum supported version.
        // 2. of the last changeset in each collection to see if the version of the database requires upgrading.
        verify { minChangeSet.number }
        verify { lastPreSplitChangeSet.number }
        verify { maxChangeSet.number }

        confirmVerified(copyFile)
        preSplitChangeSets.forEach { confirmVerified(it) }
        postSplitChangeSets.forEach { confirmVerified(it) }
    }

    @Test
    internal fun `only runs changesets if required`() {
        currentVersion = maxSupportedVersion - 1

        preSplitChangeSets.forEach { clearMocks(it, answers = false) }
        postSplitChangeSets.forEach { clearMocks(it, answers = false) }

        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)

        verify(exactly = 1) { copyFile(any(), any(), any()) }

        // Should check the changeset number in the following cases:
        // 1. of the last changeset in preSplitChangeSets to confirm we do not need them.
        // 2. of the last changeset in postSplitChangeSet to confirm the database requires upgrading.
        // 3. of the last changeset in postSplitChangeSet for logging (used to be the max supported version, but can't be with the split).
        // 4. of each changeset in the postSplitChangeSet to determine if it should be run.
        // 5. of the changesets that are actually run for logging.
        // 6. of the changesets that are actually run for updating the database version.
        verifySequence {
            lastPreSplitChangeSet.number
            maxChangeSet.number
            maxChangeSet.number

            firstPostSplitChangeSet.number
            postSplitChangeSet.number
            maxChangeSet.number

            maxChangeSet.number
            maxChangeSet.preCommandHooks
            maxChangeSet.commands
            maxChangeSet.postCommandHooks

            maxChangeSet.number
        }

        // Others in preSplitChangeSets should not have been used at all.
        confirmVerified(preSplitChangeSet)
    }

    @Test
    internal fun `reports obsolete database version`() {
        mockkStatic("com.zepben.evolve.database.sqlite.extensions.StatementExtensionsKt") {
            currentVersion = minSupportedVersion - 2

            expect {
                upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
            }.toThrow<UpgradeRunner.UpgradeException>()
                .withMessage(
                    "Failed to execute upgrade scripts. Unable to upgrade obsolete database version [v${minSupportedVersion - 2}], upgrading a " +
                        "database before v${minSupportedVersion - 1} is unsupported. Please generate a new database from the source system using an updated " +
                        "migrator."
                )

            verify(exactly = 1) { tableVersion.getVersion(any()) }
        }
    }

    @Test
    internal fun `reports futuristic database version`() {
        mockkStatic("com.zepben.evolve.database.sqlite.extensions.StatementExtensionsKt") {
            currentVersion = maxSupportedVersion + 1

            expect {
                upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
            }.toThrow<UpgradeRunner.UpgradeException>()
                .withMessage(
                    "Failed to execute upgrade scripts. Selected database is a newer version [v${maxSupportedVersion + 1}] than the supported " +
                        "version [v${maxSupportedVersion}]. Either update the version of the server you are using, or downgrade the migrator."
                )

            verify(exactly = 1) { tableVersion.getVersion(any()) }
        }
    }

    @Test
    internal fun `reports missing version numbers`() {
        mockkStatic("com.zepben.evolve.database.sqlite.extensions.StatementExtensionsKt") {
            every { tableVersion.getVersion(any()) } returns null

            expect {
                upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
            }.toThrow<UpgradeRunner.UpgradeException>()
                .withMessage(
                    "Failed to execute upgrade scripts. Invalid EWB database detected, unable to read the version number from the database. Please " +
                        "ensure you only have databases in the EWB data directory that have been generated by a working migrator."
                )

            verify(exactly = 1) { tableVersion.getVersion(any()) }
        }
    }

    @Test
    internal fun `reports database connection issues`() {
        val exceptionMessage = "some error"
        val exception = SQLException(exceptionMessage)
        every { connectionProvider(any()) } throws exception

        expect {
            upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
        }.toThrow<UpgradeRunner.UpgradeException>()
            .withMessage(exceptionMessage)
            .exception.apply {
                assertThat(cause, equalTo(exception))
            }
    }

    @Test
    internal fun `reports sql exceptions`() {
        val exception = SQLException("sql message")
        every { connection.prepareStatement(any()) } throws exception

        currentVersion = minSupportedVersion - 1

        expect {
            upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
        }.toThrow<UpgradeRunner.UpgradeException>()
            .withMessage("Failed to execute upgrade scripts. sql message")
    }

    @Test
    internal fun `create appropriately named backup`() {
        // NOTE: We have to use a version above the split to avoid checking for clones created in the split processing.
        val version = splitVersion + 1

        currentVersion = version
        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)

        currentVersion = version
        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database.db"), DatabaseType.NETWORK_MODEL)

        currentVersion = version
        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("with/path/database.sqlite"), DatabaseType.NETWORK_MODEL)

        verifySequence {
            copyFile(Paths.get("database"), Paths.get("database-v$version"), StandardCopyOption.REPLACE_EXISTING)
            copyFile(Paths.get("database.db"), Paths.get("database-v$version.db"), StandardCopyOption.REPLACE_EXISTING)
            copyFile(Paths.get("with/path/database.sqlite"), Paths.get("with/path/database-v$version.sqlite"), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    @Test
    internal fun `reports database backup io errors`() {
        currentVersion = minSupportedVersion - 1

        val exception = IOException("io error")
        every { copyFile(any(), any(), any()) } throws exception

        expect {
            upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
        }.toThrow<UpgradeRunner.UpgradeException>()
            .withMessage("Failed to create database backup. io error")
    }

    @Test
    internal fun `reports database backup security exceptions`() {
        currentVersion = minSupportedVersion - 1

        val exception = SecurityException("security error")
        every { copyFile(any(), any(), any()) } throws exception

        expect {
            upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
        }.toThrow<UpgradeRunner.UpgradeException>()
            .withMessage("Failed to create database backup. security error")
    }

    @Test
    internal fun `propagates unknown exceptions`() {
        val exception = Exception("unknown exception")
        every { connection.prepareStatement(any()) } throws exception
        currentVersion = minSupportedVersion - 1

        expect {
            upgradeRunner.connectAndUpgrade("driver:database", Paths.get("database"), DatabaseType.NETWORK_MODEL)
        }.toThrowAny().exception.also {
            assertThat(it, equalTo(exception))
        }
    }

    @Test
    internal fun `only runs changesets on appropriate database`() {
        val custOnly = Change(listOf("c-1", "c-2"), setOf(DatabaseType.CUSTOMER))
        val diagOnly = Change(listOf("d-1", "d-2"), setOf(DatabaseType.DIAGRAM))
        val netOnly = Change(listOf("n-1", "n-2"), setOf(DatabaseType.NETWORK_MODEL))
        val custDiag = Change(listOf("cd-1", "cd-2"), setOf(DatabaseType.CUSTOMER, DatabaseType.DIAGRAM))
        val custNet = Change(listOf("cn-1", "cn-2"), setOf(DatabaseType.CUSTOMER, DatabaseType.NETWORK_MODEL))
        val diagNet = Change(listOf("dn-1", "dn-2"), setOf(DatabaseType.DIAGRAM, DatabaseType.NETWORK_MODEL))

        every { maxChangeSet.commands } returns listOf(custOnly, diagOnly, netOnly, custDiag, custNet, diagNet)

        clearAllMocks(answers = false)
        currentVersion = maxSupportedVersion - 1

        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("something-customers.sqlite"), DatabaseType.CUSTOMER)
        validateChangesExecuted(listOf(custOnly, custDiag, custNet))

        clearAllMocks(answers = false)
        currentVersion = maxSupportedVersion - 1

        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("something-diagrams.sqlite"), DatabaseType.DIAGRAM)
        validateChangesExecuted(listOf(diagOnly, custDiag, diagNet))

        clearAllMocks(answers = false)
        currentVersion = maxSupportedVersion - 1

        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("something-network-model.sqlite"), DatabaseType.NETWORK_MODEL)
        validateChangesExecuted(listOf(netOnly, custNet, diagNet))
    }

    @Test
    internal fun `creates split database when upgrading for the split version`() {
        // This test is here as the first implementation didn't run the split if none of the pre-split changesets were applied, even when the version
        // required the split.
        currentVersion = splitVersion

        upgradeRunner.connectAndUpgrade("driver:database", Paths.get("something-network-model.sqlite"), DatabaseType.NETWORK_MODEL)

        verifySequence {
            copyFile(Paths.get("something-network-model.sqlite"), Paths.get("something-network-model-v$splitVersion.sqlite"), any())
            copyFile(Paths.get("something-network-model.sqlite"), Paths.get("something-customers.sqlite"), any())
            copyFile(Paths.get("something-network-model.sqlite"), Paths.get("something-diagrams.sqlite"), any())
        }
    }

    private fun changeSetOf(num: Int) = mockk<ChangeSet> {
        every { number } returns num
        every { preCommandHooks } returns emptyList()
        every { commands } returns listOf(Change(listOf("$num-1", "$num-2"), setOf(DatabaseType.CUSTOMER, DatabaseType.DIAGRAM, DatabaseType.NETWORK_MODEL)))
        every { postCommandHooks } returns emptyList()
    }

    private fun Path.databaseOfType(type: DatabaseType, extra: String = ""): Boolean =
        toString().databaseOfType(type, extra)

    private fun String.databaseOfType(type: DatabaseType, extra: String = ""): Boolean =
        contains("${type.fileDescriptor}$extra.sqlite")

    // NOTE: We pass the changeset number instead of getting it off the ChangeSet to avoid using the mockk.
    private fun validateChangeSetExecuted(changeSet: ChangeSet, changeSetNumber: Int) {
        // Check if we need to run the changeset.
        changeSet.number

        // Log that we are running the changeset.
        changeSet.number

        // Turn off any foreign key processing to improve speed (even though we don't have any).
        statement.executeUpdate("PRAGMA foreign_keys=OFF")

        // Run any pre-command hooks
        changeSet.preCommandHooks

        // Check the commands were retrieved.
        changeSet.commands

        // These statements are the contents of the commands, so make sure they were executed.
        statement.executeUpdate("$changeSetNumber-1")
        statement.executeUpdate("$changeSetNumber-2")

        // Run any post-command hooks
        changeSet.postCommandHooks

        // Update the version number in the database
        changeSet.number
        tableVersion.VERSION
        versionColumn.queryIndex
        preparedStatement.setInt(1, changeSetNumber)
        preparedStatement.executeUpdate()

        // Commit the transaction for this changeset.
        statement.executeUpdate("PRAGMA foreign_key_check")
        statement.executeUpdate("COMMIT")

        // Start a new transaction for the next changeset, turning foreign keys back on to prevent issues in turning  them off.
        statement.executeUpdate("BEGIN TRANSACTION")
        statement.executeUpdate("PRAGMA foreign_keys=ON")
    }

    private fun upgradeRealFile(fileNameSource: String, type: DatabaseType) {
        systemErr.unmute()

        val databaseFile = Files.readString(Path.of("src", "test", "resources", fileNameSource)).trim().trim('"')

        UpgradeRunner().connectAndUpgrade("jdbc:sqlite:$databaseFile", Paths.get(databaseFile), type)
    }

    private fun MockKVerificationScope.validateConnectionToDatabase(targetType: DatabaseType) {
        // Connects to the new database.
        connectionProvider(match { it.databaseOfType(targetType) })

        // Configure batch processing on the connection.
        connection.createStatement()
        statement.executeUpdate("PRAGMA journal_mode = OFF")
        statement.executeUpdate("PRAGMA synchronous = OFF")
        statement.close()
        connection.autoCommit = false
    }

    private fun MockKVerificationScope.validatePreSplitChangesets() {
        validateUpdateRequiredChecks(DatabaseType.NETWORK_MODEL, lastPreSplitChangeSet, minSupportedVersion - 1, splitVersion)

        // Take a backup
        copyFile(
            match { it.databaseOfType(DatabaseType.NETWORK_MODEL) },
            match { it.databaseOfType(DatabaseType.NETWORK_MODEL, "-v${minSupportedVersion - 1}") },
            any()
        )

        // Prepare the version update statement.
        tableVersion.preparedUpdateSql
        connection.prepareStatement("update statement")

        // Runs the pre-split commands on the network database.
        validateChangeSetExecuted(minChangeSet, minSupportedVersion)
        validateChangeSetExecuted(preSplitChangeSet, minSupportedVersion + 1)
        validateChangeSetExecuted(lastPreSplitChangeSet, splitVersion)

        // Finished with the statements for the pre-split changes.
        preparedStatement.close()
        statement.close()
    }

    private fun MockKVerificationScope.validateCloneAndUpgrade(targetType: DatabaseType) {
        // Clones the network database.
        copyFile(match { it.databaseOfType(DatabaseType.NETWORK_MODEL) }, match { it.databaseOfType(targetType) }, any())

        validateConnectionToDatabase(targetType)
        validatePostSplitChangesets(targetType)
        validateVacuum()

        // Closes the connection as we no longer need it.
        connection.close()
    }

    private fun validatePostSplitChangesets(targetType: DatabaseType) {
        validateUpdateRequiredChecks(targetType, maxChangeSet, splitVersion, maxSupportedVersion)

        // Prepare the version update statement.
        tableVersion.preparedUpdateSql
        connection.prepareStatement("update statement")

        // Runs the post-split commands on the target database.
        validateChangeSetExecuted(firstPostSplitChangeSet, splitVersion + 1)
        validateChangeSetExecuted(postSplitChangeSet, maxSupportedVersion - 1)
        validateChangeSetExecuted(maxChangeSet, maxSupportedVersion)

        // Finished with the statements for the post-split changes.
        preparedStatement.close()
        statement.close()
    }

    private fun validateUpdateRequiredChecks(databaseType: DatabaseType, targetChangeSet: ChangeSet, fromVersion: Int, toVersion: Int) {
        // Create statement for pre-split changesets.
        connection.createStatement()

        // Check to see if we need to run any pre-split upgrades
        tableVersion.getVersion(statement)
        targetChangeSet.number

        // Check the database is not past the maximum upgrade version
        tableVersion.SUPPORTED_VERSION

        // Get target version for logging
        targetChangeSet.number
        assertThat(
            systemErr.log,
            containsString("Upgrading database 'something-${databaseType.fileDescriptor}.sqlite' from v$fromVersion to v$toVersion")
        )
    }

    private fun validateVacuum() {
        connection.createStatement()
        statement.executeUpdate("COMMIT")
        statement.executeUpdate("VACUUM")
        statement.close()
    }

    private fun validateChangesExecuted(changes: List<Change>) {
        //
        // NOTE: We are only checking the statement as the connection interactions and version updates have been checked elsewhere.
        //
        verifySequence {
            // All calls to statement and connection before we get to the actual changes.
            statement.executeUpdate("PRAGMA journal_mode = OFF")
            statement.executeUpdate("PRAGMA synchronous = OFF")
            statement.close()
            statement.close()
            statement.executeUpdate("PRAGMA foreign_keys=OFF")

            // Confirm the changes were run in order.
            changes.flatMap { it.commands }.forEach {
                statement.executeUpdate(it)
            }

            // All calls to statement after the changes.
            statement.executeUpdate("PRAGMA foreign_key_check")
            statement.executeUpdate("COMMIT")
            statement.executeUpdate("BEGIN TRANSACTION")
            statement.executeUpdate("PRAGMA foreign_keys=ON")
            statement.close()
            statement.executeUpdate("COMMIT")
            statement.executeUpdate("VACUUM")
            statement.close()
        }
    }

}
