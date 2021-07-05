/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.common.translator

import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.core.Name
import com.zepben.evolve.cim.iec61970.base.core.NameType
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.extensions.internEmpty
import com.zepben.evolve.services.network.translator.getInt
import com.zepben.evolve.services.network.translator.getMapList
import com.zepben.evolve.services.network.translator.getString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive


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

