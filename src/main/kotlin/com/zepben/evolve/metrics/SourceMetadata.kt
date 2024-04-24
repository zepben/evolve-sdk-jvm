/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

import java.time.Instant

/**
 * Metadata for a data source used in ingestion.
 *
 * @property timestamp The time the source was exported from the source system.
 * @property fileHash SHA-256 of the file, if applicable.
 */
data class SourceMetadata(
    var timestamp: Instant? = null,
    var fileHash: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SourceMetadata

        if (timestamp != other.timestamp) return false
        if (fileHash != null) {
            if (other.fileHash == null) return false
            if (!fileHash.contentEquals(other.fileHash)) return false
        } else if (other.fileHash != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp?.hashCode() ?: 0
        result = 31 * result + (fileHash?.contentHashCode() ?: 0)
        return result
    }

}
