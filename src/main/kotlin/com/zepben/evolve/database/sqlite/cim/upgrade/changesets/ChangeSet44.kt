/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.upgrade.changesets

import com.zepben.evolve.database.sqlite.cim.upgrade.Change
import com.zepben.evolve.database.sqlite.cim.upgrade.ChangeSet
import com.zepben.evolve.database.paths.DatabaseType

internal fun changeSet44() = ChangeSet(
    44,
    listOf(
        `Create current_transformer_info table`,
        `Create potential_transformer_info table`,
        `Create current_transformers table`,
        `Create potential_transformers table`
    )
)

@Suppress("ObjectPropertyName")
private val `Create current_transformer_info table` = Change(
    listOf(
        """
        CREATE TABLE current_transformer_info (
            mrid TEXT NOT NULL,
            name TEXT NOT NULL,
            description TEXT NOT NULL,
            num_diagram_objects INTEGER NOT NULL,
            accuracy_class TEXT NULL,
            accuracy_limit NUMBER NULL,
            core_count INTEGER NULL,
            ct_class TEXT NULL,
            knee_point_voltage INTEGER NULL,
            max_ratio_denominator NUMBER NULL,
            max_ratio_numerator NUMBER NULL,
            nominal_ratio_denominator NUMBER NULL,
            nominal_ratio_numerator NUMBER NULL,
            primary_ratio NUMBER NULL,
            rated_current INTEGER NULL,
            secondary_fls_rating
            INTEGER NULL,
            secondary_ratio
            NUMBER NULL,
            usage TEXT NULL
        );
        """.trimIndent(),
        "CREATE UNIQUE INDEX current_transformer_info_mrid ON current_transformer_info (mrid);",
        "CREATE INDEX current_transformer_info_name ON current_transformer_info (name);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Create potential_transformer_info table` = Change(
    listOf(
        """
        CREATE TABLE potential_transformer_info (
            mrid TEXT NOT NULL,
            name TEXT NOT NULL,
            description TEXT NOT NULL,
            num_diagram_objects INTEGER NOT NULL,
            accuracy_class TEXT NULL,
            nominal_ratio_denominator NUMBER NULL,
            nominal_ratio_numerator NUMBER NULL,
            primary_ratio NUMBER NULL,
            pt_class TEXT NULL,
            rated_voltage INTEGER NULL,
            secondary_ratio NUMBER NULL
        );
        """.trimIndent(),
        "CREATE UNIQUE INDEX potential_transformer_info_mrid ON potential_transformer_info (mrid);",
        "CREATE INDEX potential_transformer_info_name ON potential_transformer_info (name);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Create current_transformers table` = Change(
    listOf(
        """
        CREATE TABLE current_transformers (
            mrid TEXT NOT NULL,
            name TEXT NOT NULL,
            description TEXT NOT NULL,
            num_diagram_objects INTEGER NOT NULL,
            location_mrid TEXT NULL,
            num_controls INTEGER NOT NULL,
            normally_in_service BOOLEAN,
            in_service BOOLEAN,
            terminal_mrid TEXT NULL,
            current_transformer_info_mrid TEXT NULL,
            core_burden INTEGER NULL
        );
        """.trimIndent(),
        "CREATE UNIQUE INDEX current_transformers_mrid ON current_transformers (mrid);",
        "CREATE INDEX current_transformers_name ON current_transformers (name);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)

@Suppress("ObjectPropertyName")
private val `Create potential_transformers table` = Change(
    listOf(
        """
        CREATE TABLE potential_transformers (
            mrid TEXT NOT NULL,
            name TEXT NOT NULL,
            description TEXT NOT NULL,
            num_diagram_objects INTEGER NOT NULL,
            location_mrid TEXT NULL,
            num_controls INTEGER NOT NULL,
            normally_in_service BOOLEAN,
            in_service BOOLEAN,
            terminal_mrid TEXT NULL,
            potential_transformer_info_mrid TEXT NULL,
            type TEXT NULL
        );
        """.trimIndent(),
        "CREATE UNIQUE INDEX potential_transformers_mrid ON potential_transformers (mrid);",
        "CREATE INDEX potential_transformers_name ON potential_transformers (name);"
    ),
    targetDatabases = setOf(DatabaseType.NETWORK_MODEL)
)
