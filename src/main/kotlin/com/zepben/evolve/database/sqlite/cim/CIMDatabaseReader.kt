/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim

import com.zepben.evolve.database.sqlite.cim.metadata.MetadataCollectionReader
import com.zepben.evolve.database.sqlite.common.TableVersion
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.common.meta.MetadataCollection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection

/**
 * A base class for reading objects from one of our CIM databases.
 *
 * @property connection The connection to the database.
 * @property metadataReader The reader for the [MetadataCollection] included in the database.
 * @property serviceReader The reader for the [BaseService] supported by the database.
 * @property service The [BaseService] that will be populated by the [BaseServiceReader]. Used for post-processing.
 * @property databaseDescription The description of the database for logging (e.g. filename).
 * @property tableVersion The version table object for checking the database version number.
 *
 * @property logger The [Logger] to use for this reader.
 */
abstract class CimDatabaseReader(
    private val connection: Connection,
    private val metadataReader: MetadataCollectionReader,
    private val serviceReader: BaseServiceReader,
    protected open val service: BaseService,
    private val databaseDescription: String,
    private val tableVersion: TableVersion
) {

    protected val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var hasBeenUsed: Boolean = false

    private val supportedVersion = tableVersion.supportedVersion

    /**
     * Customisable function for performing actions after the database has been loaded.
     */
    protected open fun postLoad(): Boolean {
        logger.info("Ensuring all references resolved...")
        service.unresolvedReferences().forEach {
            throw IllegalStateException(
                "Unresolved references found in ${service.name} service after load - this should not occur. Failing reference was from " +
                    "${it.from.typeNameAndMRID()} resolving ${it.resolver.toClass.simpleName} ${it.toMrid}"
            )
        }
        logger.info("Unresolved references were all resolved during load.")

        return true
    }

    /**
     * Load the database.
     */
    fun load(): Boolean =
        try {
            require(!hasBeenUsed) { "You can only use the database reader once." }
            hasBeenUsed = true

            preLoad()
                && loadFromReaders()
                && postLoad()
        } catch (e: Exception) {
            logger.error("Unable to load database: " + e.message)
            false
        }

    private fun preLoad(): Boolean =
        try {
            connection.createStatement().use { statement ->
                val version = tableVersion.getVersion(statement)
                if (version == supportedVersion) {
                    logger.info("Loading from database version v$version")
                    true
                } else {
                    logger.error(formatVersionError(version))
                    false
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to connect to the database for reading: " + e.message, e)
            false
        }

    private fun loadFromReaders() =
        metadataReader.load() and
            serviceReader.load()

    private fun formatVersionError(version: Int?): String =
        when {
            version == null -> "Failed to read the version number form the selected database. Are you sure it is a EWB database?"
            version < supportedVersion -> unexpectedVersion(version, "Consider using the UpgradeRunner if you wish to support this database.")
            else -> unexpectedVersion(version, "You need to use a newer version of the SDK to load this database.")
        }


    private fun unexpectedVersion(version: Int?, action: String) =
        "Unable to load from database $databaseDescription [found v$version, expected v$supportedVersion]. $action"

}
