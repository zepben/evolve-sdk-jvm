/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.translator

import com.zepben.evolve.cim.geojson.Feature
import com.zepben.evolve.cim.geojson.FeatureCollection
import com.zepben.evolve.cim.geojson.Geometry
import com.zepben.evolve.cim.iec61968.assetinfo.*
import com.zepben.evolve.cim.iec61968.assets.AssetInfo
import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.common.PositionPoint
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.translator.MissingPropertyException
import com.zepben.evolve.services.common.translator.identifiedObjectToCim
import com.zepben.evolve.services.network.NetworkService
import kotlinx.serialization.json.JsonElement


/************ IEC61968 ASSET INFO ************/

fun cableInfoToCim(feature: Feature, networkService: NetworkService): CableInfo =
    CableInfo(feature.id).apply {
        wireInfoToCim(feature, this, networkService)
    }

fun overheadWireInfoToCim(feature: Feature, networkService: NetworkService): OverheadWireInfo =
    OverheadWireInfo(feature.id).apply {
        wireInfoToCim(feature, this, networkService)
    }

fun powerTransformerInfoToCim(feature: Feature, networkService: NetworkService): PowerTransformerInfo =
    PowerTransformerInfo(feature.id).apply {
        feature.getStringList("transformerTankInfoIds")?.forEach {
            networkService.resolveOrDeferReference(Resolvers.transformerTankInfo(this), it)
        }
        assetInfoToCim(feature, this, networkService)
    }

fun transformerEndInfoToCim(feature: Feature, networkService: NetworkService): TransformerEndInfo =
    TransformerEndInfo(feature.id).apply {
        connectionKind = feature.getString("connectionKind")?.let { WindingConnection.valueOf(it) } ?: WindingConnection.UNKNOWN_WINDING
        emergencyS = feature.getInt("emergencyS") ?: 0
        endNumber = feature.getInt("endNumber") ?: 0
        insulationU = feature.getInt("insulationU") ?: 0
        phaseAngleClock = feature.getInt("phaseAngleClock") ?: 0
        r = feature.getDouble("r")
        ratedS = feature.getInt("ratedS") ?: 0
        ratedU = feature.getInt("ratedU") ?: 0
        shortTermS = feature.getInt("shortTermS") ?: 0

        networkService.resolveOrDeferReference(Resolvers.transformerTankInfo(this), feature.getString("transformerTankInfoId"))
        networkService.resolveOrDeferReference(Resolvers.transformerStarImpedance(this), feature.getString("transformerStarImpedanceId"))
        networkService.resolveOrDeferReference(Resolvers.energisedEndNoLoadTests(this), feature.getString("energisedEndNoLoadTestsId"))
        networkService.resolveOrDeferReference(Resolvers.energisedEndShortCircuitTests(this), feature.getString("energisedEndShortCircuitTestsId"))
        networkService.resolveOrDeferReference(Resolvers.groundedEndShortCircuitTests(this), feature.getString("groundedEndShortCircuitTestsId"))
        networkService.resolveOrDeferReference(Resolvers.openEndOpenCircuitTests(this), feature.getString("openEndOpenCircuitTestsId"))
        networkService.resolveOrDeferReference(Resolvers.energisedEndOpenCircuitTests(this), feature.getString("energisedEndOpenCircuitTestsId"))

        assetInfoToCim(feature, this, networkService)
    }

fun transformerTankInfoToCim(feature: Feature, networkService: NetworkService): TransformerTankInfo =
    TransformerTankInfo(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.powerTransformerInfo(this), feature.getString("powerTransformerInfoId"))
        feature.getStringList("transformerEndInfoIds")?.forEach {
            networkService.resolveOrDeferReference(Resolvers.transformerEndInfo(this), it)
        }
        assetInfoToCim(feature, this, networkService)
    }

fun wireInfoToCim(feature: Feature, cim: WireInfo, networkService: NetworkService): WireInfo =
    cim.apply {
        ratedCurrent = feature.getInt("ratedCurrent") ?: 0
        material = feature.getString("material")?.let { WireMaterialKind.valueOf(it) } ?: WireMaterialKind.UNKNOWN
        assetInfoToCim(feature, this, networkService)
    }

/************ IEC61968 ASSETS ************/

fun assetInfoToCim(feature: Feature, cim: AssetInfo, networkService: NetworkService): AssetInfo =
    cim.apply {
        identifiedObjectToCim(feature.properties, this, networkService)
    }


/************ IEC61968 COMMON ************/

fun geometryToCim(geometry: Geometry?): List<PositionPoint> =
    geometry?.coordinates?.map { coords -> PositionPoint(coords[0], coords[1]) } ?: emptyList()


fun locationToCim(networkService: NetworkService, refereeMRID: String, positionPoints: List<PositionPoint>): Location =
    Location("$refereeMRID-loc").apply {
        positionPoints.forEach(::addPoint)
        networkService.add(this)
    }

/************ IEC61970 CORE ************/

fun conductingEquipmentToCim(feature: Feature, cim: ConductingEquipment, networkService: NetworkService): ConductingEquipment =
    cim.apply {
        // Note: Terminals are handled in conductorToCim
        feature.getInt("nominalVoltage")?.let { nv ->
            networkService.get<BaseVoltage>("$nv-bv")?.let { bv ->
                this.baseVoltage = bv
            } ?: BaseVoltage("$nv-bv").also { bv ->
                bv.nominalVoltage = nv
                this.baseVoltage = bv
                networkService.add(bv)
            }
        }

        equipmentToCim(feature, this, networkService)
    }


fun equipmentToCim(feature: Feature, cim: Equipment, networkService: NetworkService): Equipment =
    cim.apply {
        inService = feature.getBoolean("inService") ?: true
        normallyInService = feature.getBoolean("normallyInService") ?: true

        feature.getStringList("equipmentContainerIds")?.forEach { equipmentContainerMRID ->
            networkService.resolveOrDeferReference(Resolvers.containers(this), equipmentContainerMRID)
        }

        feature.getStringList("usagePointIds")?.forEach { usagePointMRID ->
            networkService.resolveOrDeferReference(Resolvers.usagePoints(this), usagePointMRID)
        }

        feature.getStringList("operationalRestrictionIds")?.forEach { operationalRestrictionMRID ->
            networkService.resolveOrDeferReference(Resolvers.operationalRestrictions(this), operationalRestrictionMRID)
        }

        feature.getStringList("currentFeederIds")?.forEach { currentFeederMRID ->
            networkService.resolveOrDeferReference(Resolvers.currentFeeders(this), currentFeederMRID)
        }

        powerSystemResourceToCim(feature, this, networkService)
    }


fun powerSystemResourceToCim(feature: Feature, cim: PowerSystemResource, networkService: NetworkService): PowerSystemResource =
    cim.apply {
        // NOTE: assetInfoMRId will be handled by classes that use it with specific types.
        numControls = 0 // unused
        location = locationToCim(networkService, feature.id, geometryToCim(feature.geometry))

        identifiedObjectToCim(feature.properties, this, networkService)
    }

/************ IEC61970 WIRES ************/

fun acLineSegmentToCim(feature: Feature, networkService: NetworkService, connectivity: MutableList<Connectivity>): AcLineSegment =
    AcLineSegment(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.perLengthSequenceImpedance(this), feature.getString("perLengthSequenceImpedanceId"))
        conductorToCim(feature, this, networkService, connectivity)
    }

fun breakerToCim(feature: Feature, networkService: NetworkService): Breaker =
    Breaker(feature.id).apply {
        protectedSwitchToCim(feature, this, networkService)
    }

fun conductorToCim(feature: Feature, cim: Conductor, networkService: NetworkService, connectivity: MutableList<Connectivity>): Conductor =
    cim.apply {
        length = feature.getDouble("length")
        networkService.resolveOrDeferReference(Resolvers.assetInfo(this), feature.getString("assetInfoId"))

        feature.getString("fromConductingEquipment")?.let { from ->
            feature.getString("toConductingEquipment")?.let { to ->
                connectivity.add(Connectivity(this, from, to, feature.getString("phases")?.let { PhaseCode.valueOf(it) } ?: PhaseCode.ABC))
            } ?: throw MissingPropertyException("toConductingEquipment")
        } ?: throw MissingPropertyException("fromConductingEquipment")

        conductingEquipmentToCim(feature, cim, networkService)
    }

fun disconnectorToCim(feature: Feature, networkService: NetworkService): Disconnector =
    Disconnector(feature.id).apply {
        switchToCim(feature, this, networkService)
    }

fun fuseToCim(feature: Feature, networkService: NetworkService): Fuse =
    Fuse(feature.id).apply {
        switchToCim(feature, this, networkService)
    }

fun jumperToCim(feature: Feature, networkService: NetworkService): Jumper =
    Jumper(feature.id).apply {
        switchToCim(feature, this, networkService)
    }

fun loadBreakSwitchToCim(feature: Feature, networkService: NetworkService): LoadBreakSwitch =
    LoadBreakSwitch(feature.id).apply {
        protectedSwitchToCim(feature, this, networkService)
    }

fun perLengthLineParameterToCim(feature: Feature, cim: PerLengthLineParameter, networkService: NetworkService): PerLengthLineParameter =
    cim.apply {
        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun perLengthImpedanceToCim(feature: Feature, cim: PerLengthImpedance, networkService: NetworkService): PerLengthImpedance =
    cim.apply {
        perLengthLineParameterToCim(feature, cim, networkService)
    }

fun perLengthSequenceImpedanceToCim(feature: Feature, networkService: NetworkService): PerLengthSequenceImpedance =
    PerLengthSequenceImpedance(feature.id).apply {
        r = feature.getDouble("r")
        x = feature.getDouble("x")
        r0 = feature.getDouble("r0")
        x0 = feature.getDouble("x0")
        bch = feature.getDouble("bch")
        gch = feature.getDouble("gch")
        b0ch = feature.getDouble("b0Ch")
        g0ch = feature.getDouble("g0Ch")

        perLengthImpedanceToCim(feature, this, networkService)
    }

fun powerTransformerToCim(feature: Feature, networkService: NetworkService): PowerTransformer =
    PowerTransformer(feature.id).apply {
        feature.getMapList("ends")?.forEach { end ->
            powerTransformerEndToCim(end, this, networkService)
        }

        vectorGroup = feature.getString("vectorGroup")?.let { VectorGroup.valueOf(it) } ?: VectorGroup.UNKNOWN
        transformerUtilisation = feature.getDouble("transformerUtilisation")
        networkService.resolveOrDeferReference(Resolvers.assetInfo(this), feature.getString("assetInfoId"))

        conductingEquipmentToCim(feature, this, networkService)
    }

fun powerTransformerEndToCim(end: Map<String, JsonElement>, pt: PowerTransformer, networkService: NetworkService): PowerTransformerEnd {
    val id = end["id"] ?: throw MissingPropertyException("TransformerEnd id for ${pt.mRID} was missing")
    return PowerTransformerEnd(id.toString()).apply {
        ratedS = end.getInt("ratedS") ?: 0
        ratedU = end.getInt("ratedU") ?: 0
        r = end.getDouble("r")
        r0 = end.getDouble("r0")
        x = end.getDouble("x")
        x0 = end.getDouble("x0")
        connectionKind = end.getString("connectionKind")?.let { WindingConnection.valueOf(it) } ?: WindingConnection.UNKNOWN_WINDING
        b = end.getDouble("b")
        b0 = end.getDouble("b0")
        g = end.getDouble("g")
        g0 = end.getDouble("g0")
        phaseAngleClock = end.getInt("phaseAngleClock") ?: 0

        transformerEndToCim(end, this, pt, networkService)
    }
}

fun protectedSwitchToCim(feature: Feature, cim: ProtectedSwitch, networkService: NetworkService): ProtectedSwitch =
    cim.apply {
        switchToCim(feature, cim, networkService)
    }

fun recloserToCim(feature: Feature, networkService: NetworkService): Recloser =
    Recloser(feature.id).apply {
        protectedSwitchToCim(feature, this, networkService)
    }

fun switchToCim(feature: Feature, cim: Switch, networkService: NetworkService): Switch =
    cim.apply {
        feature.getBoolean("normallyOpen")?.let { setNormallyOpen(it) }
        feature.getBoolean("open")?.let { setOpen(it) }
        // when unganged support is added
        // normalOpen = feature.getInt("normalOpen")
        // open = feature.getInt("open")
        conductingEquipmentToCim(feature, this, networkService)
    }


fun transformerEndToCim(end: Map<String, JsonElement>, cim: TransformerEnd, pt: PowerTransformer, networkService: NetworkService): TransformerEnd =
    cim.apply {
        // TODO: handle BaseVoltage, StarImpedance, RatioTapChanger
        endNumber = end.getInt("endNumber") ?: throw MissingPropertyException("TransformerEnd endNumber for ${pt.mRID} was missing")
        terminal = pt.getTerminal(endNumber)
        grounded = end.getBoolean("grounded") ?: false
        rGround = end.getDouble("rGround")
        xGround = end.getDouble("xGround")
        identifiedObjectToCim(end, this, networkService)
    }

fun createTerminal(networkService: NetworkService, ce: ConductingEquipment, phaseCode: PhaseCode = PhaseCode.ABC): Terminal =
    (ce.numTerminals() + 1).let { sn ->
        Terminal(ce.mRID + "-t" + sn).apply {
            conductingEquipment = ce
            sequenceNumber = sn
            ce.addTerminal(this)
            networkService.add(this)
        }
    }

fun connect(networkService: NetworkService, t1: Terminal, t2: Terminal) {
    if (t2.isConnected())
        networkService.connect(t1, t2)
    else {
        val cn = "cn-" + t1.mRID
        networkService.connect(t1, cn)
        networkService.connect(t2, cn)
    }
}

//fun NetworkService.addFromGeojson(clazz: KClass<AcLineSegment>, geojson: Feature): AcLineSegment? = tryAddOrNull(acLineSegmentToCim(geojson, this))
//
//fun <T : IdentifiedObject> NetworkService.addFromGeojson(clazz: KClass<T>, geojson: Feature): T? {
//    return when (clazz) {
//        AcLineSegment::class -> tryAddOrNull(clazz.cast(acLineSegmentToCim(geojson, this)))
//        Breaker::class -> tryAddOrNull(breakerToCim(geojson, this))
//        CableInfo::class -> tryAddOrNull(cableInfoToCim(geojson, this))
//        Disconnector::class -> tryAddOrNull(disconnectorToCim(geojson, this))
//        Fuse::class -> tryAddOrNull(fuseToCim(geojson, this))
//        Jumper::class -> tryAddOrNull(jumperToCim(geojson, this))
//        LoadBreakSwitch::class -> tryAddOrNull(loadBreakSwitchToCim(geojson, this))
//        OverheadWireInfo::class -> tryAddOrNull(overheadWireInfoToCim(geojson, this))
//        PerLengthSequenceImpedance::class -> tryAddOrNull(perLengthSequenceImpedanceToCim(geojson, this))
//        PowerTransformer::class -> tryAddOrNull(powerTransformerToCim(geojson, this))
//        Recloser::class -> tryAddOrNull(recloserToCim(geojson, this))
//        else -> throw IllegalArgumentException("Serialiasing class ${clazz.simpleName} from Geojson is not currently supported")
//    }
//}

fun convertGeojsonToCim(featureCollection: FeatureCollection, networkService: NetworkService) {
    val connectivity = mutableListOf<Connectivity>()

    featureCollection.features.forEach { feature ->
        val clazz = feature.getString("class") ?: throw MissingPropertyException("Feature ${feature.id} missing required property 'class'")

        networkService.apply {
            when (clazz) {
                "AcLineSegment" -> tryAddOrNull(acLineSegmentToCim(feature, this, connectivity))
                "Breaker" -> tryAddOrNull(breakerToCim(feature, this))
                "CableInfo" -> tryAddOrNull(cableInfoToCim(feature, this))
                "Disconnector" -> tryAddOrNull(disconnectorToCim(feature, this))
                "Fuse" -> tryAddOrNull(fuseToCim(feature, this))
                "Jumper" -> tryAddOrNull(jumperToCim(feature, this))
                "LoadBreakSwitch" -> tryAddOrNull(loadBreakSwitchToCim(feature, this))
                "OverheadWireInfo" -> tryAddOrNull(overheadWireInfoToCim(feature, this))
                "PerLengthSequenceImpedance" -> tryAddOrNull(perLengthSequenceImpedanceToCim(feature, this))
                "PowerTransformer" -> tryAddOrNull(powerTransformerToCim(feature, this))
                "Recloser" -> tryAddOrNull(recloserToCim(feature, this))
                else -> throw IllegalArgumentException("Serialiasing class $clazz from feature is not currently supported")
            }
        } // ?: todo: handle metadata/hv_feeder?

    }

    createAndConnectTerminals(networkService, connectivity)
}


fun createAndConnectTerminals(networkService: NetworkService, connectivity: List<Connectivity>) {
    connectivity.forEach { c ->
        networkService.get<ConductingEquipment>(c.fromEquip)?.let { from ->
            networkService.get<ConductingEquipment>(c.toEquip)?.let { to ->
                connect(networkService, createTerminal(networkService, c.conductor, c.conductorPhaseCode), createTerminal(networkService, from))
                connect(networkService, createTerminal(networkService, c.conductor, c.conductorPhaseCode), createTerminal(networkService, to))
            }
        }
    }
}