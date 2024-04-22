/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.paths

import com.zepben.testutils.exception.ExpectException.Companion.expect
import io.mockk.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate

class EwbDataFilePathsTest {

    private val today = LocalDate.now()
    private val baseDir = Paths.get("/some/path/to/ewb/data")

    private val createDirectories = mockk<(Path) -> Path>().also { every { it(any()) } answers { firstArg() } }
    private val isDirectory = mockk<(Path) -> Boolean>().also { every { it(any()) } returns true }
    private val exists = mockk<(Path) -> Boolean>().also { every { it(any()) } returns true }
    private val listFiles = mockk<(Path) -> Iterator<Path>>().also { every { it(any()) } answers { emptyList<Path>().iterator() } }


    private val ewbPaths = EwbDataFilePaths(baseDir, createPath = false, createDirectories, isDirectory, exists, listFiles)

    @Test
    internal fun `constructor coverage`() {
        // Coverage of default values being used by primary constructor.
        EwbDataFilePaths(Paths.get("."))

        // Coverage of secondary constructor.
        EwbDataFilePaths(".")
        EwbDataFilePaths(".", createPath = true)
    }

    @Test
    internal fun `accessor coverage`() {
        assertThat(ewbPaths.baseDir, equalTo(baseDir))
    }

    @Test
    internal fun `validates directory is valid at construction`() {
        verifySequence {
            isDirectory(baseDir)
        }
        confirmVerified(createDirectories, isDirectory, exists)

        every { isDirectory(any()) } returns false

        expect { EwbDataFilePaths(baseDir, createPath = false, createDirectories, isDirectory, exists) }
            .toThrow<IllegalArgumentException>()
            .withMessage("baseDir must be a directory")
    }

    @Test
    internal fun `creates missing root directory at construction if requested`() {
        // Clear the calls from the member variable call to the constructor.
        clearMocks(isDirectory, answers = false)

        EwbDataFilePaths(baseDir, createPath = true, createDirectories, isDirectory, exists)

        verifySequence {
            createDirectories(baseDir)
            isDirectory(baseDir)
        }
        confirmVerified(createDirectories, isDirectory, exists)
    }

    @Test
    internal fun `formats paths`() {
        assertThat(ewbPaths.customer(today), equalTo(baseDir.datedPath(today, "customers")))
        assertThat(ewbPaths.diagram(today), equalTo(baseDir.datedPath(today, "diagrams")))
        assertThat(ewbPaths.measurement(today), equalTo(baseDir.datedPath(today, "measurements")))
        assertThat(ewbPaths.networkModel(today), equalTo(baseDir.datedPath(today, "network-model")))
        assertThat(ewbPaths.tileCache(today), equalTo(baseDir.datedPath(today, "tile-cache")))
        assertThat(ewbPaths.energyReading(today), equalTo(baseDir.datedPath(today, "load-readings")))

        assertThat(ewbPaths.energyReadingsIndex(), equalTo(baseDir.resolve("load-readings-index.sqlite")))
        assertThat(ewbPaths.loadAggregatorMetersByDate(), equalTo(baseDir.resolve("load-aggregator-mbd.sqlite")))
        assertThat(ewbPaths.weatherReading(), equalTo(baseDir.resolve("weather-readings.sqlite")))
        assertThat(ewbPaths.resultsCache(), equalTo(baseDir.resolve("results-cache.sqlite")))
        assertThat(ewbPaths.metrics(), equalTo(baseDir.resolve("metrics.sqlite")))
    }

    @Test
    internal fun `creates date directories if they dont exist`() {
        val dateDir = baseDir.resolve(today.toString())

        assertThat(ewbPaths.createDirectories(today), equalTo(dateDir))

        confirmVerified(createDirectories)
        every { exists(any()) } returns false

        assertThat(ewbPaths.createDirectories(today), equalTo(dateDir))

        verifySequence { createDirectories(dateDir) }
    }

    @Test
    internal fun `finds specified date if it exists`() {
        validateClosest(today, 1)

        // Should return null without checking a file as they are not date based, even if the file exists.
        validateClosest(DatabaseType.ENERGY_READINGS_INDEX, null, 0)
        validateClosest(DatabaseType.LOAD_AGGREGATOR_METERS_BY_DATE, null, 0)
        validateClosest(DatabaseType.WEATHER_READING, null, 0)
        validateClosest(DatabaseType.RESULTS_CACHE, null, 0)
    }

    @Test
    internal fun `finds previous date if it exists and today is missing`() {
        // NOTE: We want to use two days ago rather than yesterday to make sure it searches more than one day.
        val twoDaysAgo = today.minusDays(2)

        // Only find files for yesterday.
        every { exists(any()) } returns false
        every { exists(match { it.toString().contains(twoDaysAgo.toString()) }) } returns true

        validateClosest(twoDaysAgo, 3)
    }

    @Test
    internal fun `doesn't find files outside the search window`() {
        val elevenDaysAgo = today.minusDays(11)

        // Only find files for 11 days ago (outside the 10-day search).
        every { exists(any()) } returns false
        every { exists(match { it.toString().contains(elevenDaysAgo.toString()) }) } returns true

        validateClosest(null, 11)
    }

    @Test
    internal fun `can search forwards in time`() {
        // NOTE: We want to use two days from now rather than tomorrow to make sure it searches more than one day. We also use
        //       three days ago to make sure it is searching outwards from the date, not into the past then the future.
        val twoDaysFromNow = today.plusDays(2)
        val threeDaysAgo = today.minusDays(3)

        // Find files for 2 days from now and 3 days ago.
        every { exists(any()) } returns false
        every { exists(match { it.toString().contains(twoDaysFromNow.toString()) }) } returns true
        every { exists(match { it.toString().contains(threeDaysAgo.toString()) }) } returns true

        validateClosest(twoDaysFromNow, 5, searchForwards = true)
    }

    @Test
    internal fun `closest date using default parameters`() {
        val tomorrow = today.plusDays(1)
        val twoDaysAgo = today.minusDays(2)

        // Find files for tomorrow and 2 days ago.
        every { exists(any()) } returns false
        every { exists(match { it.toString().contains(tomorrow.toString()) }) } returns true
        every { exists(match { it.toString().contains(twoDaysAgo.toString()) }) } returns true

        // Should find two days ago as it doesn't search forward by default.
        assertThat(ewbPaths.findClosest(DatabaseType.NETWORK_MODEL), equalTo(twoDaysAgo))
    }

    @Test
    internal fun `getAvailableDatesFor accepts date types`() {
        DatabaseType.entries.filter { it.perDate }.forEach {
            `validate getAvailableDatesFor for date type`(it)
        }
    }

    @Test
    internal fun `getAvailableDatesFor throws on non-date type`() {
        DatabaseType.entries.filter { !it.perDate }.forEach {
            expect { `validate getAvailableDatesFor for date type`(it) }
                .toThrow<IllegalStateException>()
                .withMessage("INTERNAL ERROR: Should only be calling `getAvailableDatesFor` for `perDate` files, which should all be covered above, so go ahead and add it.")
        }
    }

    private fun `validate getAvailableDatesFor for date type` (dbType: DatabaseType) {
        clearMocks(isDirectory, exists, listFiles, answers = false)

        val usableDirectories = listOf("2001-02-03", "2001-02-04", "2011-03-09")
        val emptyDirectories = listOf("2111-11-11", "2222-12-14")
        val nonDateDirectories = listOf("other_data", "2002-02-04-backup", "backup-2011-03-09")
        val nonDirectoryFiles = listOf("config.json", "other", "run.sh", "1234-11-22")

        val allFiles = (usableDirectories + emptyDirectories + nonDateDirectories + nonDirectoryFiles)
        val dateDirectories = (usableDirectories + emptyDirectories)

        every { listFiles(baseDir) } answers { allFiles.map { Paths.get(baseDir.toString(), it) }.iterator() }

        nonDirectoryFiles.forEach {
            every { isDirectory(Paths.get(baseDir.toString(), it)) } returns false
        }

        emptyDirectories.forEach {
            every { exists(baseDir.datedPath(LocalDate.parse(it), dbType.fileDescriptor)) } returns false
        }

        assertThat(ewbPaths.getAvailableDatesFor(dbType), equalTo(usableDirectories.map { LocalDate.parse(it) }))

        verifySequence {
            listFiles(baseDir)
            allFiles.forEach {
                isDirectory(Paths.get(baseDir.toString(),it))
                if (it in dateDirectories) {
                    exists(baseDir.datedPath(LocalDate.parse(it), dbType.fileDescriptor))
                }
            }
        }
    }

    @Test
    internal fun `getAvailableDatesFor() sorts the returned dates`() {
        val directories = listOf(
            Path.of("2001-02-03"),
            Path.of("2032-05-07"),
            Path.of("2009-05-09"),
            Path.of("2009-05-08")
        )

        every { listFiles(baseDir) } answers { directories.iterator() }

        assertThat(
            ewbPaths.getAvailableDatesFor(DatabaseType.NETWORK_MODEL), equalTo(
                listOf(
                    LocalDate.parse("2001-02-03"),
                    LocalDate.parse("2009-05-08"),
                    LocalDate.parse("2009-05-09"),
                    LocalDate.parse("2032-05-07"),
                )
            )
        )
    }

    @Test
    internal fun `getNetworkModelDatabases coverage`() {
        clearMocks(isDirectory, exists, listFiles, answers = false)

        val hasNetworkDirectories = listOf("2111-02-03")
        val emptyDirectories = listOf("2555-11-11")

        val allDirectories = (hasNetworkDirectories + emptyDirectories)

        every { listFiles(baseDir) } answers { allDirectories.map { Paths.get(baseDir.toString(), it) }.iterator() }

        every { exists(any()) } returns false
        hasNetworkDirectories.forEach {
            every { exists(baseDir.datedPath(LocalDate.parse(it), DatabaseType.NETWORK_MODEL.fileDescriptor)) } returns true
        }

        assertThat(ewbPaths.getNetworkModelDatabases(), equalTo(hasNetworkDirectories.map { LocalDate.parse(it) }))

        verifySequence {
            listFiles(baseDir)
            allDirectories.forEach {
                isDirectory(Paths.get(baseDir.toString(), it))
                exists(baseDir.datedPath(LocalDate.parse(it), DatabaseType.NETWORK_MODEL.fileDescriptor))
            }
        }
    }

    private fun validateClosest(expectedDate: LocalDate?, expectedExistCalls: Int, searchForwards: Boolean = false) {
        DatabaseType.entries.filter { it.perDate }.forEach {
            validateClosest(it, expectedDate, expectedExistCalls, searchForwards)
        }
    }

    private fun validateClosest(type: DatabaseType, expectedDate: LocalDate?, expectedExistCalls: Int, searchForwards: Boolean = false) {
        // Clear the calls, so we are not affected by previous calls that may not have been validated.
        clearMocks(exists, answers = false)

        assertThat(ewbPaths.findClosest(type, 10, today, searchForwards), equalTo(expectedDate))

        verify(exactly = expectedExistCalls) { exists(any()) }
    }

    private fun Path.datedPath(date: LocalDate, name: String): Path =
        resolve(date.toString()).resolve("$date-$name.sqlite")
}
