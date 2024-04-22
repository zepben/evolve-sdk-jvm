/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.paths

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import kotlin.io.path.name


/**
 * Provides paths to all the various data files / folders used by EWB.
 *
 * @param baseDir The root directory of the EWB data structure.
 * @param createPath Create the root directory (and any missing parent folders) if it does not exist.
 */
class EwbDataFilePaths @JvmOverloads constructor(
    val baseDir: Path,
    createPath: Boolean = false,
    private val createDirectories: (Path) -> Path = { Files.createDirectories(it) },
    private val isDirectory: (Path) -> Boolean = { Files.isDirectory(it) },
    private val exists: (Path) -> Boolean = { Files.exists(it) },
    private val listFiles: (Path) -> Iterator<Path> = { Files.list(it).iterator() }
) {

    init {
        if (createPath)
            createDirectories(baseDir)

        require(isDirectory(baseDir)) { "baseDir must be a directory" }
    }

    @JvmOverloads
    constructor(baseDir: String, createPath: Boolean = false) : this(Paths.get(baseDir), createPath)

    /**
     * Determine the path to the "customers" database for the specified date.
     *
     * @param date The date to use for the "customers" database.
     * @return The path to the "customers" database for the specified date.
     */
    fun customer(date: LocalDate): Path = date.toDatedPath(DatabaseType.CUSTOMER.fileDescriptor)

    /**
     * Determine the path to the "diagrams" database for the specified date.
     *
     * @param date The date to use for the "diagrams" database.
     * @return The path to the "diagrams" database for the specified date.
     */
    fun diagram(date: LocalDate): Path = date.toDatedPath(DatabaseType.DIAGRAM.fileDescriptor)

    /**
     * Determine the path to the "measurements" database for the specified date.
     *
     * @param date The date to use for the "measurements" database.
     * @return The path to the "measurements" database for the specified date.
     */
    fun measurement(date: LocalDate): Path = date.toDatedPath(DatabaseType.MEASUREMENT.fileDescriptor)

    /**
     * Determine the path to the "network model" database for the specified date.
     *
     * @param date The date to use for the "network model" database.
     * @return The path to the "network model" database for the specified date.
     */
    fun networkModel(date: LocalDate): Path = date.toDatedPath(DatabaseType.NETWORK_MODEL.fileDescriptor)

    /**
     * Determine the path to the "tile cache" database for the specified date.
     *
     * @param date The date to use for the "tile cache" database.
     * @return The path to the "tile cache" database for the specified date.
     */
    fun tileCache(date: LocalDate): Path = date.toDatedPath(DatabaseType.TILE_CACHE.fileDescriptor)

    /**
     * Determine the path to the "energy readings" database for the specified date.
     *
     * @param date The date to use for the "energy readings" database.
     * @return The path to the "energy readings" database for the specified date.
     */
    fun energyReading(date: LocalDate): Path = date.toDatedPath(DatabaseType.ENERGY_READING.fileDescriptor)

    /**
     * Determine the path to the "energy readings index" database.
     *
     * @return The path to the "energy readings index" database.
     */
    fun energyReadingsIndex(): Path = baseDir.resolve(DatabaseType.ENERGY_READINGS_INDEX.fileDescriptor + ".sqlite")

    /**
     * Determine the path to the "load aggregator meters-by-date" database.
     *
     * @return The path to the "load aggregator meters-by-date" database.
     */
    fun loadAggregatorMetersByDate(): Path = baseDir.resolve(DatabaseType.LOAD_AGGREGATOR_METERS_BY_DATE.fileDescriptor + ".sqlite")

    /**
     * Determine the path to the "weather readings" database.
     *
     * @return The path to the "weather readings" database.
     */
    fun weatherReading(): Path = baseDir.resolve(DatabaseType.WEATHER_READING.fileDescriptor + ".sqlite")

    /**
     * Determine the path to the "results cache" database.
     *
     * @return The path to the "results cache" database.
     */
    fun resultsCache(): Path = baseDir.resolve(DatabaseType.RESULTS_CACHE.fileDescriptor + ".sqlite")

    /**
     * Determine the path to the "metrics" database.
     *
     * @return The path to the "metrics" database.
     */
    fun metrics(): Path = baseDir.resolve(DatabaseType.METRICS.fileDescriptor + ".sqlite")

    /**
     * Create the directories required to have a valid path for the specified date.
     *
     * @param date The [LocalDate] required in the path.
     * @return The [Path] to the directory for the [date].
     */
    @Throws(IOException::class)
    fun createDirectories(date: LocalDate): Path {
        val datePath = baseDir.resolve(date.toString())
        return if (exists(datePath))
            datePath
        else
            createDirectories(datePath)
    }

    /**
     * Find the closest date with a usable database of the specified type.
     *
     * @param type The type of database to search for.
     * @param maxDaysToSearch The maximum number of days to search for a valid database.
     * @param date The target date. Defaults to today.
     * @param searchForwards Indicates the search should also look forwards in time from [date] for a valid file. Defaults to reverse search only.
     *
     * @return The closest [LocalDate] to [date] with a valid database of [type] within the search parameters, or null if no valid database was found.
     */
    @JvmOverloads
    fun findClosest(type: DatabaseType, maxDaysToSearch: Int = 999999, date: LocalDate = LocalDate.now(), searchForwards: Boolean = false): LocalDate? {
        // We do not want to return a date for non date based files.
        if (!type.perDate)
            return null

        if (checkExists(type, date))
            return date

        var offset = 1L
        while (offset <= maxDaysToSearch) {
            val previousDate = date.minusDays(offset)
            if (checkExists(type, previousDate))
                return previousDate

            if (searchForwards) {
                val forwardDate = date.plusDays(offset)
                if (checkExists(type, forwardDate))
                    return forwardDate
            }
            ++offset
        }

        return null
    }

    /**
     * Check if a database of the specified type and date exists.
     *
     * @param type The type of database to search for.
     * @param date The date to check.
     *
     * @return True if a database of the specified [type] and [date] exits in the date path.
     */
    private fun checkExists(type: DatabaseType, date: LocalDate): Boolean {
        val modelPath = when (type) {
            DatabaseType.CUSTOMER -> customer(date)
            DatabaseType.DIAGRAM -> diagram(date)
            DatabaseType.MEASUREMENT -> measurement(date)
            DatabaseType.NETWORK_MODEL -> networkModel(date)
            DatabaseType.TILE_CACHE -> tileCache(date)
            DatabaseType.ENERGY_READING -> energyReading(date)
            else -> throw IllegalStateException("INTERNAL ERROR: Should only be calling `checkExists` for `perDate` files, which should all be covered above, so go ahead and add it.")
        }
        return exists(modelPath)
    }

    private fun LocalDate.toDatedPath(file: String): Path =
        toString().let { dateStr -> Paths.get(baseDir.toString(), dateStr, "$dateStr-$file.sqlite") }

    internal fun getAvailableDatesFor(type: DatabaseType): List<LocalDate> {
        if (!type.perDate)
            throw IllegalStateException("INTERNAL ERROR: Should only be calling `getAvailableDatesFor` for `perDate` files, which should all be covered above, so go ahead and add it.")

        return listFiles(baseDir)
            .asSequence()
            .filter { isDirectory(it) }
            .mapNotNull { runCatching { LocalDate.parse(it.name) }.getOrNull() }
            .filter { exists(it.toDatedPath(type.fileDescriptor)) }
            .sorted()
            .toList()
    }

    /**
     * Find available network-model databases in data path.
     *
     * @return A list of [LocalDate]'s for which network-model databases exist in the data path.
     */
    fun getNetworkModelDatabases(): List<LocalDate> = getAvailableDatesFor(DatabaseType.NETWORK_MODEL)
}
