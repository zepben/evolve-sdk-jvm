/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.diagram.translator

import com.zepben.evolve.cim.geojson.Feature
import com.zepben.evolve.cim.iec61970.base.diagramlayout.*
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.UNKNOWN_DOUBLE
import com.zepben.evolve.services.common.translator.MissingPropertyException
import com.zepben.evolve.services.diagram.DiagramService
import com.zepben.evolve.services.network.translator.getDouble
import kotlinx.serialization.json.JsonElement


fun diagramToCim(feature: Feature, diagramService: DiagramService): Diagram =
    Diagram(feature.id).apply {

        feature.getStringList("diagramObjectIds")?.forEach { diagramObjectId ->
            diagramService.resolveOrDeferReference(Resolvers.diagramObjects(this), diagramObjectId)
        }
        orientationKind = feature.getString("orientationKind")?.let { OrientationKind.valueOf(it) } ?: OrientationKind.POSITIVE
        diagramStyle = feature.getString("diagramStyle")?.let { DiagramStyle.valueOf(it) } ?: DiagramStyle.GEOGRAPHIC
    }

fun diagramObjectToCim(feature: Feature, diagramService: DiagramService): DiagramObject =
    DiagramObject(feature.id).apply {
        diagramService.resolveOrDeferReference(Resolvers.diagram(this), feature.getString("diagramId"))
        diagram?.addDiagramObject(this)
        rotation = feature.getDouble("rotation") ?: 0.0
        identifiedObjectMRID = feature.getString("identifiedObjectId").takeIf { it?.isNotBlank() == true }
        style = feature.getString("diagramObjectStyle").takeIf { it?.isNotBlank() == true }
        feature.getMapList("diagramObjectPoints")?.forEach { addPoint(diagramObjectPointToCim(it, mRID)) }
    }

fun diagramObjectPointToCim(dop: Map<String, JsonElement>, diagramObjectId: String): DiagramObjectPoint =
    dop.getDouble("x")?.let { x ->
        if (x == UNKNOWN_DOUBLE)
            throw MissingPropertyException("DiagramObjectPoint missing x on $diagramObjectId")
        dop.getDouble("y")?.let { y ->
            if (y == UNKNOWN_DOUBLE)
                throw MissingPropertyException("DiagramObjectPoint missing y on $diagramObjectId")
            DiagramObjectPoint(x, y)
        } ?: throw MissingPropertyException("DiagramObjectPoint missing x on $diagramObjectId")
    } ?: throw MissingPropertyException("DiagramObjectPoint missing x on $diagramObjectId")