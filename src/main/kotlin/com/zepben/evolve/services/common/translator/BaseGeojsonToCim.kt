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
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.extensions.internEmpty
import com.zepben.evolve.services.network.translator.getInt
import com.zepben.evolve.services.network.translator.getMapList
import com.zepben.evolve.services.network.translator.getString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/************ IEC61968 COMMON ************/

fun documentToCim(feature: Feature, cim: Document, baseService: BaseService): Document =
    cim.apply {
        title = feature.getString("title") ?: ""
        createdDateTime = feature.getString("createdDateTime")?.let { LocalDateTime.parse(it).toInstant(ZoneOffset.UTC) }
        authorName = feature.getString("authorName") ?: ""
        type = feature.getString("type") ?: ""
        status = feature.getString("status") ?: ""
        comment = feature.getString("comment") ?: ""
        identifiedObjectToCim(feature.properties, this, baseService)
    }

fun organisationToCim(feature: Feature, baseService: BaseService): Organisation =
    Organisation(feature.id).apply {
        identifiedObjectToCim(feature.properties, this, baseService)
    }

fun organisationRoleToCim(feature: Feature, cim: OrganisationRole, baseService: BaseService): OrganisationRole =
    cim.apply {
        baseService.resolveOrDeferReference(Resolvers.organisation(this), feature.getString("organisationId"))
        identifiedObjectToCim(feature.properties, this, baseService)
    }

/************ IEC61970 CORE ************/
fun identifiedObjectToCim(feature: Map<String, JsonElement>, cim: IdentifiedObject, baseService: BaseService): IdentifiedObject =
    cim.apply {
        name = (feature.getString("name") ?: "").internEmpty()
        description = (feature.getString("description") ?: "").internEmpty()
        numDiagramObjects = feature.getInt("numDiagramObjects") ?: 0

        feature.getMapList("names")?.forEach { nameObj ->
            nameObj["name"]?.let { n ->
                if (!n.jsonPrimitive.isString)
                    throw IllegalArgumentException("name $n of ${cim.mRID} must be a string")

                nameObj["type"]?.let { t ->
                    if (!t.jsonPrimitive.isString)
                        throw IllegalArgumentException("type $t of ${cim.mRID} must be a string")

                    addName(nameToCim(n.jsonPrimitive.content, t.jsonPrimitive.content, this, baseService))
                } ?: throw MissingPropertyException("type")
            } ?: throw MissingPropertyException("name")
        }
    }

fun nameToCim(name: String, type: String, cim: IdentifiedObject, baseService: BaseService): Name {
    val nameType = baseService.getNameType(type) ?: NameType(type).also { baseService.addNameType(it) }
    return nameType.getOrAddName(name, cim)
}

fun nameTypeToCim(feature: Feature, baseService: BaseService): NameType =
    (feature.getString("name") ?: "").let { name ->
        (baseService.getNameType(name) ?: NameType(name).also { baseService.addNameType(it) })
            .apply { description = feature.getString("description") ?: "" }
    }
