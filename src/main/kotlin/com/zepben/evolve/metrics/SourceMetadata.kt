/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.metrics

/**
 * Metadata for a data source used in ingestion.
 */
data class SourceMetadata(
    val fileHash: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SourceMetadata

        if (fileHash != null) {
            if (other.fileHash == null) return false
            if (!fileHash.contentEquals(other.fileHash)) return false
        } else if (other.fileHash != null) return false

        return true
    }

    override fun hashCode(): Int {
        return fileHash?.contentHashCode() ?: 0
    }
}
