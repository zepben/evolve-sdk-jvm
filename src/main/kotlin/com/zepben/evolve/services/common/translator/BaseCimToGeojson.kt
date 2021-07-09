/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.common.translator

import com.zepben.evolve.cim.geojson.Feature
import com.zepben.evolve.cim.iec61968.common.Document
import com.zepben.evolve.cim.iec61968.common.Organisation
import com.zepben.evolve.cim.iec61968.common.OrganisationRole
import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.core.Name
import com.zepben.evolve.cim.iec61970.base.core.NameType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalDateTime
import java.time.ZoneOffset


/************ IEC61968 COMMON ************/

fun documentToGeojson(feature: Feature, cim: Document): Feature =
    feature.apply {
        cim.title.takeUnless { it.isBlank() }?.let { put("title", it) }
        cim.createdDateTime?.let { put("createdDateTime", LocalDateTime.ofInstant(it, ZoneOffset.UTC).toString()) }
        cim.authorName.takeUnless { it.isBlank() }?.let { put("authorName", it) }
        cim.type.takeUnless { it.isBlank() }?.let { put("type", it) }
        cim.status.takeUnless { it.isBlank() }?.let { put("status", it) }
        cim.comment.takeUnless { it.isBlank() }?.let { put("comment", it) }

        identifiedObjectToGeojson(this, cim)
    }

fun organisationToGeojson(cim: Organisation): Feature =
    Feature(cim.mRID).apply {
        identifiedObjectToGeojson(this, cim)
    }

fun organisationRoleToGeojson(feature: Feature, cim: OrganisationRole): Feature =
    feature.apply {
        cim.organisation?.let { put("organisationId", it.mRID) }

        identifiedObjectToGeojson(this, cim)
    }

/************ IEC61970 CORE ************/
fun identifiedObjectToGeojson(feature: Feature, cim: IdentifiedObject): Feature =
    feature.apply {
        cim.name.takeUnless { it.isBlank() }?.let { put("name", it) }
        cim.description.takeUnless { it.isBlank() }?.let { put("description", it) }
        put("numDiagramObjects", cim.numDiagramObjects)

        cim.names.map { nameToGeojson(it) }.let { put("names", JsonArray(it)) }
    }

fun nameToGeojson(cim: Name): JsonObject = JsonObject(mapOf("name" to JsonPrimitive(cim.name), "type" to JsonPrimitive(cim.type.name)))


fun nameTypeToGeojson(cim: NameType): JsonObject = JsonObject(mapOf("name" to JsonPrimitive(cim.name), "description" to JsonPrimitive(cim.description)))
