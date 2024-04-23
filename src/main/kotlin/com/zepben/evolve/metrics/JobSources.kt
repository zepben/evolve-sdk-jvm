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
 * A collection of data sources for a job. Missing metadata is automatically created.
 */
class JobSources : AutoMap<String, SourceMetadata>() {

    override fun defaultValue(): SourceMetadata = SourceMetadata()

}
