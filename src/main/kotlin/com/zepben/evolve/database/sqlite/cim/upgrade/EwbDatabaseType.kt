/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade

/**
 * Indicates the type of EWB database being referenced.
 *
 * @property fileDescriptor The descriptive part of the filename for this database type.
 */
enum class EwbDatabaseType(
    val fileDescriptor: String
) {

    /**
     * Refers to a network database.
     */
    NETWORK("network-model"),

    /**
     * Refers to a customer database.
     */
    CUSTOMER("customers"),

    /**
     * Refers to a diagram database.
     */
    DIAGRAM("diagrams")

}
