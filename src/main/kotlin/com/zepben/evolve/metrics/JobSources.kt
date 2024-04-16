/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

typealias JobSources = MutableMap<String, SourceMetadata>

fun jobSourcesWithDefault() = mutableMapOf<String, SourceMetadata>().withDefault { SourceMetadata() }
