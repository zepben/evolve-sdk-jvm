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
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Loop
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.UNKNOWN_DOUBLE
import com.zepben.evolve.services.common.UNKNOWN_INT
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
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

fun noLoadTestToCim(feature: Feature, networkService: NetworkService): NoLoadTest =
    NoLoadTest(feature.id).apply {
        energisedEndVoltage = feature.getInt("energisedEndVoltage").takeUnless { it == UNKNOWN_INT }
        excitingCurrent = feature.getDouble("excitingCurrent").takeUnless { it == UNKNOWN_DOUBLE }
        excitingCurrentZero = feature.getDouble("excitingCurrentZero").takeUnless { it == UNKNOWN_DOUBLE }
        loss = feature.getInt("loss").takeUnless { it == UNKNOWN_INT }
        lossZero = feature.getInt("lossZero").takeUnless { it == UNKNOWN_INT }
        transformerTestToCim(feature, this, networkService)
    }

fun openCircuitTestToCim(feature: Feature, networkService: NetworkService): OpenCircuitTest =
    OpenCircuitTest(feature.id).apply {
        energisedEndStep = feature.getInt("energisedEndStep").takeUnless { it == UNKNOWN_INT }
        energisedEndVoltage = feature.getInt("energisedEndVoltage").takeUnless { it == UNKNOWN_INT }
        openEndStep = feature.getInt("openEndStep").takeUnless { it == UNKNOWN_INT }
        openEndVoltage = feature.getInt("openEndVoltage").takeUnless { it == UNKNOWN_INT }
        phaseShift = feature.getDouble("phaseShift").takeUnless { it == UNKNOWN_DOUBLE }
        transformerTestToCim(feature, this, networkService)
    }


fun powerTransformerInfoToCim(feature: Feature, networkService: NetworkService): PowerTransformerInfo =
    PowerTransformerInfo(feature.id).apply {
        feature.getStringList("transformerTankInfoIds")?.forEach {
            networkService.resolveOrDeferReference(Resolvers.transformerTankInfo(this), it)
        }
        assetInfoToCim(feature, this, networkService)
    }

fun shortCircuitTestToCim(feature: Feature, networkService: NetworkService): ShortCircuitTest =
    ShortCircuitTest(feature.id).apply {
        current = feature.getDouble("current").takeUnless { it == UNKNOWN_DOUBLE }
        energisedEndStep = feature.getInt("energisedEndStep").takeUnless { it == UNKNOWN_INT }
        groundedEndStep = feature.getInt("groundedEndStep").takeUnless { it == UNKNOWN_INT }
        leakageImpedance = feature.getDouble("leakageImpedance").takeUnless { it == UNKNOWN_DOUBLE }
        leakageImpedanceZero = feature.getDouble("leakageImpedanceZero").takeUnless { it == UNKNOWN_DOUBLE }
        loss = feature.getInt("loss").takeUnless { it == UNKNOWN_INT }
        lossZero = feature.getInt("lossZero").takeUnless { it == UNKNOWN_INT }
        power = feature.getInt("power").takeUnless { it == UNKNOWN_INT }
        voltage = feature.getDouble("voltage").takeUnless { it == UNKNOWN_DOUBLE }
        voltageOhmicPart = feature.getDouble("voltageOhmicPart").takeUnless { it == UNKNOWN_DOUBLE }
        transformerTestToCim(feature, this, networkService)
    }

fun transformerEndInfoToCim(feature: Feature, networkService: NetworkService): TransformerEndInfo =
    TransformerEndInfo(feature.id).apply {
        connectionKind = feature.getString("connectionKind")?.let { WindingConnection.valueOf(it) } ?: WindingConnection.UNKNOWN_WINDING
        emergencyS = feature.getInt("emergencyS").takeUnless { it == UNKNOWN_INT }
        endNumber = feature.getInt("endNumber") ?: 0
        insulationU = feature.getInt("insulationU").takeUnless { it == UNKNOWN_INT }
        phaseAngleClock = feature.getInt("phaseAngleClock").takeUnless { it == UNKNOWN_INT }
        r = feature.getDouble("r")
        ratedS = feature.getInt("ratedS").takeUnless { it == UNKNOWN_INT }
        ratedU = feature.getInt("ratedU").takeUnless { it == UNKNOWN_INT }
        shortTermS = feature.getInt("shortTermS").takeUnless { it == UNKNOWN_INT }

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

fun transformerTestToCim(feature: Feature, cim: TransformerTest, networkService: NetworkService): TransformerTest =
    cim.apply {
        basePower = feature.getInt("basePower").takeUnless { it == UNKNOWN_INT }
        temperature = feature.getDouble("temperature").takeUnless { it == UNKNOWN_DOUBLE }
        identifiedObjectToCim(feature, this, networkService)
    }

fun wireInfoToCim(feature: Feature, cim: WireInfo, networkService: NetworkService): WireInfo =
    cim.apply {
        ratedCurrent = feature.getInt("ratedCurrent").takeUnless { it == UNKNOWN_INT }
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


fun connectivityNodeContainerToCim(feature: Feature, cim: ConnectivityNodeContainer, networkService: NetworkService): ConnectivityNodeContainer =
    cim.apply {
        powerSystemResourceToCim(feature, cim, networkService)
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

fun equipmentContainerToCim(feature: Feature, cim: EquipmentContainer, networkService: NetworkService): EquipmentContainer =
    cim.apply {
        connectivityNodeContainerToCim(feature, cim, networkService)
    }

fun feederToCim(feature: Feature, networkService: NetworkService, headEquipment: MutableMap<String, String>): Feeder =
    Feeder(feature.id).apply {
        headEquipment[feature.id] = feature.getString("normalHeadEquipmentId") ?: throw MissingPropertyException("normalHeadEquipmentId")
        networkService.resolveOrDeferReference(Resolvers.normalEnergizingSubstation(this), feature.getString("normalEnergizingSubstationId"))

        equipmentContainerToCim(feature, this, networkService)
    }

fun geographicalRegionToCim(feature: Feature, networkService: NetworkService): GeographicalRegion =
    GeographicalRegion(feature.id).apply {
        feature.getStringList("subGeographicalRegionIds")?.forEach { subGeographicalRegionMRID ->
            networkService.resolveOrDeferReference(Resolvers.subGeographicalRegions(this), subGeographicalRegionMRID)
        }
        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun powerSystemResourceToCim(feature: Feature, cim: PowerSystemResource, networkService: NetworkService): PowerSystemResource =
    cim.apply {
        // NOTE: assetInfoMRId will be handled by classes that use it with specific types.
        numControls = 0 // unused
        location = locationToCim(networkService, feature.id, geometryToCim(feature.geometry))

        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun siteToCim(feature: Feature, networkService: NetworkService): Site =
    Site(feature.id).apply {
        equipmentContainerToCim(feature, this, networkService)
    }

fun subGeographicalRegionToCim(feature: Feature, networkService: NetworkService): SubGeographicalRegion =
    SubGeographicalRegion(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.geographicalRegion(this), feature.getString("geographicalRegionId"))

        feature.getStringList("substationIds")?.forEach { substationMRID ->
            networkService.resolveOrDeferReference(Resolvers.substations(this), substationMRID)
        }

        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun substationToCim(feature: Feature, networkService: NetworkService): Substation =
    Substation(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.subGeographicalRegion(this), feature.getString("subGeographicalRegionId"))

        feature.getStringList("normalEnergizedFeederIds")?.forEach { normalEnergizedFeederMRID ->
            networkService.resolveOrDeferReference(Resolvers.normalEnergizingFeeders(this), normalEnergizedFeederMRID)
        }
        feature.getStringList("loopIds")?.forEach { loopMRID ->
            networkService.resolveOrDeferReference(Resolvers.loops(this), loopMRID)
        }
        feature.getStringList("normalEnergizedLoopIds")?.forEach { normalEnergizedLoopMRID ->
            networkService.resolveOrDeferReference(Resolvers.normalEnergizedLoops(this), normalEnergizedLoopMRID)
        }
        feature.getStringList("circuitIds")?.forEach { circuitMRID ->
            networkService.resolveOrDeferReference(Resolvers.circuits(this), circuitMRID)
        }

        equipmentContainerToCim(feature, this, networkService)
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

fun lineToCim(feature: Feature, cim: Line, networkService: NetworkService): Line =
    cim.apply {
        equipmentContainerToCim(feature, this, networkService)
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

/************ IEC61970 InfIEC61970 Feeder ************/

fun circuitToCim(feature: Feature, networkService: NetworkService, endEquipment: MutableMap<String, MutableList<String>>): Circuit =
    Circuit(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.loop(this), feature.getString("loopId"))

        feature.getStringList("endEquipmentIds")?.forEach {
            endEquipment.computeIfAbsent(feature.id) { mutableListOf() }.add(it)
        } ?: throw MissingPropertyException("endEquipmentIds")

        feature.getStringList("endSubstationIds")?.forEach { endSubstationMRID ->
            networkService.resolveOrDeferReference(Resolvers.endSubstation(this), endSubstationMRID)
        }

        lineToCim(feature, this, networkService)
    }

fun loopToCim(feature: Feature, networkService: NetworkService): Loop =
    Loop(feature.id).apply {
        feature.getStringList("circuitId")?.forEach { circuitMRID ->
            networkService.resolveOrDeferReference(Resolvers.circuits(this), circuitMRID)
        }

        feature.getStringList("substationIds")?.forEach { substationMRID ->
            networkService.resolveOrDeferReference(Resolvers.substations(this), substationMRID)
        }

        feature.getStringList("normalEnergizingSubstationIds")?.forEach { normalEnergizingSubstationMRID ->
            networkService.resolveOrDeferReference(Resolvers.normalEnergizingSubstations(this), normalEnergizingSubstationMRID)
        }

        identifiedObjectToCim(feature.properties, this, networkService)
    }


/************ MISC ****************/

internal fun createTerminal(networkService: NetworkService, ce: ConductingEquipment, phaseCode: PhaseCode = PhaseCode.ABC): Terminal =
    (ce.numTerminals() + 1).let { sn ->
        Terminal(ce.mRID + "-t" + sn).apply {
            conductingEquipment = ce
            sequenceNumber = sn
            phases = phaseCode
            ce.addTerminal(this)
            networkService.add(this)
        }
    }

internal fun connect(networkService: NetworkService, t1: Terminal, t2: Terminal) {
    if (t2.isConnected())
        networkService.connect(t1, t2)
    else {
        val cn = "cn-" + t1.mRID
        networkService.connect(t1, cn)
        networkService.connect(t2, cn)
    }
}

fun convertGeojsonToCim(featureCollection: FeatureCollection, networkService: NetworkService) {
    val connectivity = mutableListOf<Connectivity>()
    val headEquipment = mutableMapOf<String, String>()
    val endEquipment = mutableMapOf<String, MutableList<String>>()

    featureCollection.features.forEach { feature ->
        val clazz = feature.getString("class") ?: throw MissingPropertyException("Feature ${feature.id} missing required property 'class'")

        networkService.apply {
            when (clazz) {
                "Feeder" -> tryAddOrNull(feederToCim(feature, this, headEquipment))
                "GeographicalRegion" -> tryAddOrNull(geographicalRegionToCim(feature, this))
                "Site" -> tryAddOrNull(siteToCim(feature, this))
                "SubGeographicalRegion" -> tryAddOrNull(subGeographicalRegionToCim(feature, this))
                "Substation" -> tryAddOrNull(substationToCim(feature, this))
                "Circuit" -> tryAddOrNull(circuitToCim(feature, this, endEquipment))
                "Loop" -> tryAddOrNull(loopToCim(feature, this))
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
        }
    }

    createAndConnectTerminals(networkService, connectivity)
    connectFeederTerminals(networkService, headEquipment)
    connectCircuitTerminals(networkService, endEquipment)
}


internal fun createAndConnectTerminals(networkService: NetworkService, connectivity: List<Connectivity>) {
    connectivity.forEach { c ->
        networkService.get<ConductingEquipment>(c.fromEquip)?.let { from ->
            networkService.get<ConductingEquipment>(c.toEquip)?.let { to ->
                connect(networkService, createTerminal(networkService, c.conductor, c.conductorPhaseCode), createTerminal(networkService, from))
                connect(networkService, createTerminal(networkService, c.conductor, c.conductorPhaseCode), createTerminal(networkService, to))
            }
        }
    }
}

internal fun connectFeederTerminals(networkService: NetworkService, headEquipment: Map<String, String>) {
    headEquipment.forEach { (feederId, equipmentId) ->
        networkService.get<Feeder>(feederId)?.apply {
            val ce = networkService.get<ConductingEquipment>(equipmentId) ?: throw MissingPropertyException("Equipment $equipmentId referenced by ${typeNameAndMRID()} was not present in the network - did it get processed?")
            normalHeadTerminal = ce.terminals.last()
            ce.addContainer(this)
            addEquipment(ce)
        } ?: throw IllegalStateException("Feeder $feederId was not present in network - it should have been added. Check previous errors.")
    }
}

internal fun connectCircuitTerminals(networkService: NetworkService, endEquipment: Map<String, List<String>>) {
    endEquipment.forEach { (circuitId, equipmentIds) ->
        networkService.get<Circuit>(circuitId)?.apply {
            equipmentIds.forEach { equipmentId ->
                val ce = networkService.get<ConductingEquipment>(equipmentId)
                    ?: throw MissingPropertyException("Equipment $equipmentId referenced by ${typeNameAndMRID()} was not present in the network - did it get processed?")

                addEndTerminal(ce.terminals.first())
                ce.addContainer(this)
                addEquipment(ce)
            }
        } ?: throw IllegalStateException("Circuit $circuitId was not present in network - it should have been added. Check previous errors.")
    }
}