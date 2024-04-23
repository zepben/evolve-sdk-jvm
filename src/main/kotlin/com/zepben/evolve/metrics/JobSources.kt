/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

/**
 * Type holding a source's name and its metadata.
 */
typealias JobSource = Map.Entry<String, SourceMetadata>

/**
 * A collection of data sources for a job.
 *
 * @property entries a collection of map entries from a source's identifier to its metadata
 */
class JobSources {

    private val sourceNameToMetadata: MutableMap<String, SourceMetadata> = mutableMapOf()

    val entries: Set<JobSource> get() = sourceNameToMetadata.entries

    /**
     * Get metadata for a source by its name. Creates default metadata if none exists for the source.
     */
    operator fun get(sourceName: String): SourceMetadata = sourceNameToMetadata.getOrPut(sourceName) { SourceMetadata() }

}
