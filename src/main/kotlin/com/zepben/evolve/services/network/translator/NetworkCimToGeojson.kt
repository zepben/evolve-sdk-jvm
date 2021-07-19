/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.translator


import com.zepben.evolve.cim.geojson.Feature
import com.zepben.evolve.cim.geojson.Geometry
import com.zepben.evolve.cim.geojson.GeometryType
import com.zepben.evolve.cim.geojson.NULL_GEOMETRY
import com.zepben.evolve.cim.iec61968.assetinfo.*
import com.zepben.evolve.cim.iec61968.assets.*
import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.common.PositionPoint
import com.zepben.evolve.cim.iec61968.common.StreetAddress
import com.zepben.evolve.cim.iec61968.metering.EndDevice
import com.zepben.evolve.cim.iec61968.metering.Meter
import com.zepben.evolve.cim.iec61968.metering.UsagePoint
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.AuxiliaryEquipment
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.FaultIndicator
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentBranch
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentEquipment
import com.zepben.evolve.cim.iec61970.base.meas.*
import com.zepben.evolve.cim.iec61970.base.scada.RemoteControl
import com.zepben.evolve.cim.iec61970.base.scada.RemotePoint
import com.zepben.evolve.cim.iec61970.base.scada.RemoteSource
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.BatteryUnit
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.PhotoVoltaicUnit
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.PowerElectronicsUnit
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.PowerElectronicsWindUnit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Loop
import com.zepben.evolve.services.common.translator.documentToGeojson
import com.zepben.evolve.services.common.translator.identifiedObjectToGeojson
import com.zepben.evolve.services.common.translator.organisationRoleToGeojson
import com.zepben.evolve.services.network.NetworkService
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive


/************ IEC61968 ASSET INFO ************/

fun cableInfoToGeojson(cim: CableInfo): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        wireInfoToGeojson(this, cim)
    }

fun overheadWireInfoToGeojson(cim: OverheadWireInfo): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        wireInfoToGeojson(this, cim)
    }

fun noLoadTestToGeojson(cim: NoLoadTest): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.energisedEndVoltage?.let { put("energisedEndVoltage", it) }
        cim.excitingCurrent?.let { put("excitingCurrent", it) }
        cim.excitingCurrentZero?.let { put("excitingCurrentZero", it) }
        cim.loss?.let { put("loss", it) }
        cim.lossZero?.let { put("lossZero", it) }

        transformerTestToGeojson(this, cim)
    }

fun openCircuitTestToGeojson(cim: OpenCircuitTest): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.energisedEndStep?.let { put("energisedEndStep", it) }
        cim.energisedEndVoltage?.let { put("energisedEndVoltage", it) }
        cim.openEndStep?.let { put("openEndStep", it) }
        cim.openEndVoltage?.let { put("openEndVoltage", it) }
        cim.phaseShift?.let { put("phaseShift", it) }

        transformerTestToGeojson(this, cim)
    }


fun powerTransformerInfoToGeojson(cim: PowerTransformerInfo): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.transformerTankInfos.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("transformerTankInfoIds", it) }

        assetInfoToGeojson(this, cim)
    }

fun shortCircuitTestToGeojson(cim: ShortCircuitTest): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.current?.let { put("current", it) }
        cim.energisedEndStep?.let { put("energisedEndStep", it) }
        cim.groundedEndStep?.let { put("groundedEndStep", it) }
        cim.leakageImpedance?.let { put("leakageImpedance", it) }
        cim.leakageImpedanceZero?.let { put("leakageImpedanceZero", it) }
        cim.loss?.let { put("loss", it) }
        cim.lossZero?.let { put("lossZero", it) }
        cim.power?.let { put("power", it) }
        cim.voltage?.let { put("voltage", it) }
        cim.voltageOhmicPart?.let { put("voltageOhmicPart", it) }

        transformerTestToGeojson(this, cim)
    }


fun transformerEndInfoToGeojson(cim: TransformerEndInfo): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        put("connectionKind", cim.connectionKind.name)
        cim.emergencyS?.let { put("emergencyS", it) }
        put("endNumber", cim.endNumber)
        cim.insulationU?.let { put("insulationU", it) }
        cim.phaseAngleClock?.let { put("phaseAngleClock", it) }
        cim.r?.let { put("r", it) }
        cim.ratedS?.let { put("ratedS", it) }
        cim.ratedU?.let { put("ratedU", it) }
        cim.shortTermS?.let { put("shortTermS", it) }

        cim.transformerTankInfo?.let { put("transformerTankInfoId", it.mRID) }
        cim.transformerStarImpedance?.let { put("transformerStarImpedanceId", it.mRID) }
        cim.energisedEndNoLoadTests?.let { put("energisedEndNoLoadTestsId", it.mRID) }
        cim.energisedEndShortCircuitTests?.let { put("energisedEndShortCircuitTestsId", it.mRID) }
        cim.groundedEndShortCircuitTests?.let { put("groundedEndShortCircuitTestsId", it.mRID) }
        cim.openEndOpenCircuitTests?.let { put("openEndOpenCircuitTestsId", it.mRID) }
        cim.energisedEndOpenCircuitTests?.let { put("energisedEndOpenCircuitTestsId", it.mRID) }

        assetInfoToGeojson(this, cim)
    }

fun transformerTankInfoToGeojson(cim: TransformerTankInfo): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.powerTransformerInfo?.let { put("powerTransformerInfoId", it.mRID) }
        cim.transformerEndInfos.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("transformerEndInfoIds", it) }

        assetInfoToGeojson(this, cim)
    }

fun transformerTestToGeojson(feature: Feature, cim: TransformerTest): Feature =
    feature.apply {
        cim.basePower?.let { put("basePower", it) }
        cim.temperature?.let { put("temperature", it) }

        identifiedObjectToGeojson(this, cim)
    }

fun wireInfoToGeojson(feature: Feature, cim: WireInfo): Feature =
    feature.apply {
        cim.ratedCurrent?.let { put("ratedCurrent", it) }
        put("material", cim.material.name)

        assetInfoToGeojson(this, cim)
    }

/************ IEC61968 ASSETS ************/

fun assetToGeojson(feature: Feature, cim: Asset): Feature =
    feature.apply {
        cim.location?.let { locationToGeojson(feature, it, "location") }
        cim.organisationRoles.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("organisationRoleIds", it) }

        identifiedObjectToGeojson(this, cim)
    }

fun assetContainerToGeojson(feature: Feature, cim: AssetContainer): Feature =
    feature.apply { assetToGeojson(this, cim) }

fun assetInfoToGeojson(feature: Feature, cim: AssetInfo): Feature =
    feature.apply { identifiedObjectToGeojson(this, cim) }

fun assetOrganisationRoleToGeojson(feature: Feature, cim: AssetOrganisationRole): Feature =
    feature.apply { organisationRoleToGeojson(this, cim) }

fun assetOwnerToGeojson(cim: AssetOwner): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        assetOrganisationRoleToGeojson(this, cim)
    }

fun poleToGeojson(cim: Pole): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.classification.takeUnless { it.isBlank() }?.let { put("classification", it) }
        cim.streetlights.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("streetlightIds", it) }
        structureToGeojson(this, cim)
    }

fun streetlightToGeojson(cim: Streetlight): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        put("lampKind", cim.lampKind.name)
        cim.lightRating?.let { put("lightRating", it) }
        cim.pole?.let { put("poleId", it.mRID) }

        assetToGeojson(this, cim)
    }

fun structureToGeojson(feature: Feature, cim: Structure): Feature =
    feature.apply { assetContainerToGeojson(this, cim) }

/************ IEC61968 COMMON ************/

fun positionPointsToGeometry(points: List<PositionPoint>): Geometry =
    if (points.isEmpty())
        NULL_GEOMETRY
    else if (points.size == 1) {
        Geometry(GeometryType.Point, points.first().let { listOf(JsonPrimitive(it.xPosition), JsonPrimitive(it.yPosition)) })
    } else if (points.first() == points.last() && points.size > 2) {
        Geometry(GeometryType.Polygon, points.map { JsonArray(listOf(JsonPrimitive(it.xPosition), JsonPrimitive(it.yPosition))) })
    } else {
        Geometry(GeometryType.LineString, points.map { JsonArray(listOf(JsonPrimitive(it.xPosition), JsonPrimitive(it.yPosition))) })
    }

fun positionPointsToGeojson(points: List<PositionPoint>): List<JsonElement> =
    points.map { JsonArray(listOf(JsonPrimitive(it.xPosition), JsonPrimitive(it.yPosition))) }

/**
 * Populates [feature] with a json object keyed by [fieldName] containing all relevant [Location] fields.
 * Note the location field will be omitted if no fields are set.
 *
 * @param fieldName The key to use for the Location object.
 * @param includePoints Whether to include PositionPoints in the resulting location map
 */
@Suppress("ReplacePutWithAssignment")
fun locationToGeojson(feature: Feature, cim: Location, fieldName: String, includePoints: Boolean = false): Feature =
    feature.apply {
        val mainAddress = mutableMapOf<String, JsonElement>()
        mainAddress["id"] = JsonPrimitive(cim.mRID)
        cim.mainAddress?.let { streetAddressToGeojson(mainAddress, it) }

        if (includePoints)
            cim.points.takeUnless { it.isEmpty() }?.let { mainAddress.put("positionPoints", positionPointsToGeojson(it) )}

        put(fieldName, mainAddress)
    }

fun streetAddressToGeojson(obj: MutableMap<String, JsonElement>, cim: StreetAddress): MutableMap<String, JsonElement> =
    obj.apply {
        cim.postalCode.takeUnless { it.isBlank() }?.let { put("postalCode", it) }
        cim.townDetail?.let { td ->
            td.name.takeUnless { it.isBlank() }?.let { put("name", it) }
            td.stateOrProvince.takeUnless { it.isBlank() }?.let { put("stateOrProvince", it) }
        }
    }


/************ IEC61968 METERING ************/

fun endDeviceToGeojson(feature: Feature, cim: EndDevice): Feature =
    feature.apply {
        cim.usagePoints.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("usagePointIds", it) }
        cim.customerMRID?.let { put("customerId", it) }
        // We include PositionPoints in the location in this instance as the Geometry will be used for the Asset location.
        cim.serviceLocation?.let { locationToGeojson(feature, it, "serviceLocation", includePoints = true) }

        assetContainerToGeojson(this, cim)
    }

fun meterToGeojson(cim: Meter): Feature =
    Feature(cim.mRID, geometry = cim.serviceLocation?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        endDeviceToGeojson(this, cim)
    }

fun usagePointToGeojson(cim: UsagePoint): Feature =
    Feature(cim.mRID, geometry = cim.usagePointLocation?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.usagePointLocation?.let { locationToGeojson(this, it, "usagePointLocation") }
        cim.equipment.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("equipmentIds", it) }
        cim.endDevices.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("endDeviceIds", it) }

        identifiedObjectToGeojson(this, cim)
    }

/************ IEC61968 OPERATIONS ************/

fun operationalRestrictionToGeojson(cim: OperationalRestriction): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        documentToGeojson(this, cim)
    }

/************ IEC61970 AUXILIARY EQUIPMENT ************/

fun auxiliaryEquipmentToGeojson(feature: Feature, cim: AuxiliaryEquipment): Feature =
    feature.apply {
        // this can result in a number of scenarios:
        // if no terminal - no equipment will be in the geojson (fine)
        // if there's a terminal with nothing connected to it, no equipment in the geojson and terminal will be "lost" when importing the geojson (probably fine)
        // if there is connected equipment you should always get equipmentId1 and equipmentId2 as long as the ConnectivityResult comes back with not-null from
        // and to - which should always be the case, or at the very least from should be populated.
        cim.terminal?.let { terminal ->
            terminal.conductingEquipment?.let { ce ->
                NetworkService.connectedEquipment(ce).firstOrNull()?.let { cr ->
                    cr.from?.let { from -> put("equipmentId1", from.mRID) }
                    cr.to?.let { to -> put("equipmentId2", to.mRID) }
                }
            }
        }
        equipmentToGeojson(this, cim)
    }

fun faultIndicatorToGeojson(cim: FaultIndicator): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        auxiliaryEquipmentToGeojson(this, cim)
    }

/************ IEC61970 CORE ************/

fun baseVoltageToGeojson(cim: BaseVoltage): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        put("nominalVoltage", cim.nominalVoltage)
        identifiedObjectToGeojson(this, cim)
    }

fun conductingEquipmentToGeojson(feature: Feature, cim: ConductingEquipment): Feature =
    feature.apply {
        // Note: Terminals are handled in conductorToGeojson
        cim.baseVoltage?.let { put("baseVoltageId", it.mRID) }

        equipmentToGeojson(this, cim)
    }

fun connectivityNodeContainerToGeojson(feature: Feature, cim: ConnectivityNodeContainer): Feature =
    feature.apply {
        powerSystemResourceToGeojson(feature, cim)
    }

fun equipmentToGeojson(feature: Feature, cim: Equipment): Feature =
    feature.apply {
        put("inService", cim.inService)
        put("normallyInService", cim.normallyInService)

        // Indicates this equipment belongs to a given Substation or Circuit. TODO: Handle situation with >1 substation/circuit?
        cim.substations.firstOrNull()?.let { put("substationId", it.mRID) }
        cim.circuits.firstOrNull()?.let { put("substationId", it.mRID) }

        cim.containers.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("equipmentContainerIds", it) }

        cim.usagePoints.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("usagePointIds", it) }
        cim.operationalRestrictions.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("operationalRestrictionIds", it) }
        cim.currentFeeders.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("currentFeederIds", it) }

        powerSystemResourceToGeojson(this, cim)
    }

fun equipmentContainerToGeojson(feature: Feature, cim: EquipmentContainer): Feature =
    feature.apply {
        connectivityNodeContainerToGeojson(this, cim)
    }

fun feederToGeojson(cim: Feeder): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.normalHeadTerminal?.conductingEquipment?.let { ce -> put("normalHeadEquipmentId", ce.mRID) }
        cim.normalEnergizingSubstation?.let { put("normalEnergizingSubstationId", it.mRID) }

        equipmentContainerToGeojson(this, cim)
    }

fun geographicalRegionToGeojson(cim: GeographicalRegion): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.subGeographicalRegions.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("subGeographicalRegionIds", it) }
        identifiedObjectToGeojson(this, cim)
    }

fun powerSystemResourceToGeojson(feature: Feature, cim: PowerSystemResource): Feature =
    feature.apply {
        // NOTE: assetInfoMRId will be handled by classes that use it with specific types.
        put("numControls", cim.numControls)
        cim.location?.let { locationToGeojson(feature, it, "location") }

        identifiedObjectToGeojson(this, cim)
    }

fun siteToGeojson(cim: Site): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        equipmentContainerToGeojson(this, cim)
    }

fun subGeographicalRegionToGeojson(cim: SubGeographicalRegion): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.geographicalRegion?.let { put("geographicalRegionId", it.mRID) }
        cim.substations.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("substationIds", it) }

        identifiedObjectToGeojson(this, cim)
    }

fun substationToGeojson(cim: Substation): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.subGeographicalRegion?.let { put("subGeographicalRegionId", it.mRID) }

        cim.feeders.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("normalEnergizedFeederIds", it) }
        cim.loops.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("loopIds", it) }
        cim.energizedLoops.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("energizedLoopIds", it) }
        cim.circuits.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("circuitIds", it) }

        equipmentContainerToGeojson(this, cim)
    }

/************ IEC61970 BASE EQUIVALENTS ************/
fun equivalentBranchToGeojson(cim: EquivalentBranch): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.negativeR12?.let { put("negativeR12", it) }
        cim.negativeR21?.let { put("negativeR21", it) }
        cim.negativeX12?.let { put("negativeX12", it) }
        cim.negativeX21?.let { put("negativeX21", it) }
        cim.positiveR12?.let { put("positiveR12", it) }
        cim.positiveR21?.let { put("positiveR21", it) }
        cim.positiveX12?.let { put("positiveX12", it) }
        cim.positiveX21?.let { put("positiveX21", it) }
        cim.r?.let { put("r", it) }
        cim.r21?.let { put("r21", it) }
        cim.x?.let { put("x", it) }
        cim.x21?.let { put("x21", it) }
        cim.zeroR12?.let { put("zeroR12", it) }
        cim.zeroR21?.let { put("zeroR21", it) }
        cim.zeroX12?.let { put("zeroX12", it) }
        cim.zeroX21?.let { put("zeroX21", it) }
        equivalentEquipmentToGeojson(this, cim)
    }

fun equivalentEquipmentToGeojson(feature: Feature, cim: EquivalentEquipment): Feature =
    feature.apply { conductingEquipmentToGeojson(this, cim) }

/************ IEC61970 MEAS ************/

fun controlToGeojson(cim: Control): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.powerSystemResourceMRID?.let { put("powerSystemResourceId", it) }
        cim.remoteControl?.let { put("remoteControlId", it.mRID) }

        ioPointToGeojson(this, cim)
    }

fun ioPointToGeojson(feature: Feature, cim: IoPoint): Feature =
    feature.apply { identifiedObjectToGeojson(this, cim) }

fun measurementToGeojson(feature: Feature, cim: Measurement) =
    feature.apply {
        cim.powerSystemResourceMRID?.let { put("powerSystemResourceId", it) }
        cim.remoteSource?.let { put("remoteSourceId", it.mRID) }
        cim.terminalMRID?.let { put("terminalId", it) } // TODO: make this not supported for geojson?
        put("phases", cim.phases.name)
        put("unitSymbol", cim.unitSymbol.name)

        identifiedObjectToGeojson(this, cim)
    }

fun accumulatorToGeojson(cim: Accumulator): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        measurementToGeojson(this, cim)
    }

fun analogToGeojson(cim: Analog): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        put("positiveFlowIn", cim.positiveFlowIn)
        measurementToGeojson(this, cim)
    }

fun discreteToGeojson(cim: Discrete): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        measurementToGeojson(this, cim)
    }

/************ IEC61970 SCADA ************/

fun remoteControlToGeojson(cim: RemoteControl): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.control?.let { put("controlId", it.mRID) }
        remotePointToGeojson(this, cim)
    }

fun remotePointToGeojson(feature: Feature, cim: RemotePoint): Feature =
    feature.apply { identifiedObjectToGeojson(this, cim) }

fun remoteSourceToGeojson(cim: RemoteSource): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.measurement?.let { put("measurementId", it.mRID) }
        remotePointToGeojson(this, cim)
    }

/************ IEC61970 WIRES GENERATION PRODUCTION ************/

fun powerElectronicsUnitToGeojson(feature: Feature, cim: PowerElectronicsUnit): Feature =
    feature.apply {
        cim.powerElectronicsConnection?.let { put("powerElectronicsConnectionId", it.mRID) }
        cim.maxP?.let { put("maxP", it) }
        cim.minP?.let { put("minP", it) }
        equipmentToGeojson(this, cim)
    }

fun batteryUnitToGeojson(cim: BatteryUnit): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        put("batteryState", cim.batteryState.name)
        cim.ratedE?.let { put("ratedE", it) }
        cim.storedE?.let { put("storedE", it) }

        powerElectronicsUnitToGeojson(this, cim)
    }

fun photoVoltaicUnitToGeojson(cim: PhotoVoltaicUnit): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        powerElectronicsUnitToGeojson(this, cim)
    }

fun powerElectronicsWindUnitToGeojson(cim: PowerElectronicsWindUnit): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        powerElectronicsUnitToGeojson(this, cim)
    }

/************ IEC61970 WIRES ************/

fun acLineSegmentToGeojson(cim: AcLineSegment): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.perLengthSequenceImpedance?.let { put("perLengthSequenceImpedanceId", it.mRID) }
        conductorToGeojson(this, cim)
    }

fun breakerToGeojson(cim: Breaker): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        protectedSwitchToGeojson(this, cim)
    }

fun busbarSectionToGeojson(cim: BusbarSection): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        connectorToGeojson(this, cim)
    }

fun conductorToGeojson(feature: Feature, cim: Conductor): Feature =
    feature.apply {
        cim.length?.let { put("length", it) }
        cim.assetInfo?.let { put("assetInfoId", it.mRID) }

        // TODO: this currently doesn't handle non-conductors being directly connected (e.g fuses connected to transformers), so we need to decide if we want to change how we do connectivity.
        // Also, we assume all conductors have only two terminals, this needs to be explicitly documented (I don't think it currently is)
        cim.getTerminal(1)?.let { from ->
            cim.getTerminal(2)?.let { to ->
                from.conductingEquipment?.let { put("fromId", it.mRID) }
                to.conductingEquipment?.let { put("toId", it.mRID) }
            }
        }

        conductingEquipmentToGeojson(feature, cim)
    }

fun connectorToGeojson(feature: Feature, cim: Connector): Feature =
    feature.apply { conductingEquipmentToGeojson(this, cim) }

fun disconnectorToGeojson(cim: Disconnector): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        switchToGeojson(this, cim)
    }

fun energyConnectionToGeojson(feature: Feature, cim: EnergyConnection): Feature =
    feature.apply { conductingEquipmentToGeojson(feature, cim) }

fun energyConsumerToGeojson(cim: EnergyConsumer): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.phases.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("energyConsumerPhaseIds", it) }
        cim.customerCount?.let { put("customerCount", it) }
        put("grounded", cim.grounded)
        cim.p?.let { put("p", it) }
        cim.pFixed?.let { put("pFixed", it) }
        put("phaseConnection", cim.phaseConnection.name)
        cim.q?.let { put("q", it) }
        cim.qFixed?.let { put("qFixed", it) }

        energyConnectionToGeojson(this, cim)
    }

fun energyConsumerPhaseToGeojson(cim: EnergyConsumerPhase): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.energyConsumer?.let { put("energyConsumerId", it.mRID) }
        put("phase", cim.phase.name)
        cim.p?.let { put("p", it) }
        cim.pFixed?.let { put("pFixed", it) }
        cim.q?.let { put("q", it) }
        cim.qFixed?.let { put("qFixed", it) }

        powerSystemResourceToGeojson(this, cim)
    }

fun energySourceToGeojson(cim: EnergySource): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.phases.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("energySourcePhaseIds", it) }
        cim.activePower?.let { put("activePower", it) }
        cim.reactivePower?.let { put("reactivePower", it) }
        cim.voltageAngle?.let { put("voltageAngle", it) }
        cim.voltageMagnitude?.let { put("voltageMagnitude", it) }
        cim.r?.let { put("r", it) }
        cim.x?.let { put("x", it) }
        cim.pMax?.let { put("pMax", it) }
        cim.pMin?.let { put("pMin", it) }
        cim.r0?.let { put("r0", it) }
        cim.rn?.let { put("rn", it) }
        cim.x0?.let { put("x0", it) }
        cim.xn?.let { put("xn", it) }

        energyConnectionToGeojson(this, cim)
    }

fun energySourcePhaseToGeojson(cim: EnergySourcePhase): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.energySource?.let { put("energySourceId", it.mRID) }
        put("phase", cim.phase.name)

        powerSystemResourceToGeojson(this, cim)
    }

fun fuseToGeojson(cim: Fuse): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        switchToGeojson(this, cim)
    }

fun jumperToGeojson(cim: Jumper): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        switchToGeojson(this, cim)
    }

fun junctionToGeojson(cim: Junction): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        connectorToGeojson(this, cim)
    }

fun lineToGeojson(feature: Feature, cim: Line): Feature =
    feature.apply { equipmentContainerToGeojson(this, cim) }

fun linearShuntCompensatorToGeojson(cim: LinearShuntCompensator): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.b0PerSection?.let { put("b0PerSection", it) }
        cim.bPerSection?.let { put("bPerSection", it) }
        cim.g0PerSection?.let { put("g0PerSection", it) }
        cim.gPerSection?.let { put("gPerSection", it) }

        shuntCompensatorToGeojson(this, cim)
    }

fun loadBreakSwitchToGeojson(cim: LoadBreakSwitch): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        protectedSwitchToGeojson(this, cim)
    }

fun perLengthLineParameterToGeojson(feature: Feature, cim: PerLengthLineParameter): Feature =
    feature.apply { identifiedObjectToGeojson(this, cim) }

fun perLengthImpedanceToGeojson(feature: Feature, cim: PerLengthImpedance): Feature =
    feature.apply { perLengthLineParameterToGeojson(feature, cim) }

fun perLengthSequenceImpedanceToGeojson(cim: PerLengthSequenceImpedance): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.r?.let { put("r", it) }
        cim.x?.let { put("x", it) }
        cim.r0?.let { put("r0", it) }
        cim.x0?.let { put("x0", it) }
        cim.bch?.let { put("bch", it) }
        cim.gch?.let { put("gch", it) }
        cim.b0ch?.let { put("b0Ch", it) }
        cim.g0ch?.let { put("g0Ch", it) }

        perLengthImpedanceToGeojson(this, cim)
    }

fun powerElectronicsConnectionToGeojson(cim: PowerElectronicsConnection): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.units.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("powerElectronicsUnitIds", it) }
        cim.phases.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("powerElectronicsConnectionPhaseIds", it) }
        cim.maxIFault?.let { put("maxIFault", it) }
        cim.maxQ?.let { put("maxQ", it) }
        cim.minQ?.let { put("minQ", it) }
        cim.p?.let { put("p", it) }
        cim.q?.let { put("q", it) }
        cim.ratedS?.let { put("ratedS", it) }
        cim.ratedU?.let { put("ratedU", it) }

        regulatingCondEqToGeojson(this, cim)
    }

fun powerElectronicsConnectionPhaseToGeojson(cim: PowerElectronicsConnectionPhase): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.powerElectronicsConnection?.let { put("powerElectronicsConnectionId", it.mRID) }
        cim.p?.let { put("p", it) }
        put("phase", cim.phase.name)
        cim.q?.let { put("q", it) }

        powerSystemResourceToGeojson(this, cim)
    }

fun powerTransformerToGeojson(cim: PowerTransformer): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.ends.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("powerTransformerEndIds", it) }
        put("vectorGroup", cim.vectorGroup.name)
        cim.transformerUtilisation?.let { put("transformerUtilisation", it) }
        cim.assetInfo?.let { put("assetInfoId", it.mRID) }

        conductingEquipmentToGeojson(this, cim)
    }

fun powerTransformerEndToGeojson(cim: PowerTransformerEnd): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.powerTransformer?.let { put("powerTransformerId", it.mRID) }
        cim.ratedS?.let { put("ratedS", it) }
        cim.ratedU?.let { put("ratedU", it) }
        cim.r?.let { put("r", it) }
        cim.r0?.let { put("r0", it) }
        cim.x?.let { put("x", it) }
        cim.x0?.let { put("x0", it) }
        put("connectionKind", cim.connectionKind.name)
        cim.b?.let { put("b", it) }
        cim.b0?.let { put("b0", it) }
        cim.g?.let { put("g", it) }
        cim.g0?.let { put("g0", it) }
        cim.phaseAngleClock?.let { put("phaseAngleClock", it) }

        transformerEndToGeojson(this, cim)
    }

fun protectedSwitchToGeojson(feature: Feature, cim: ProtectedSwitch): Feature =
    feature.apply { switchToGeojson(feature, cim) }

fun ratioTapChangerToGeojson(cim: RatioTapChanger): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.transformerEnd?.let { put("transformerEndId", it.mRID) }
        cim.stepVoltageIncrement?.let { put("stepVoltageIncrement", it) }

        tapChangerToGeojson(this, cim)
    }

fun recloserToGeojson(cim: Recloser): Feature =
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        protectedSwitchToGeojson(this, cim)
    }

fun regulatingCondEqToGeojson(feature: Feature, cim: RegulatingCondEq): Feature =
    feature.apply {
        put("controlEnabled", cim.controlEnabled)
        energyConnectionToGeojson(this, cim)
    }

fun shuntCompensatorToGeojson(feature: Feature, cim: ShuntCompensator): Feature =
    feature.apply {
        cim.sections?.let { put("sections", it) }
        put("grounded", cim.grounded)
        cim.nomU?.let { put("nomU", it) }
        put("phaseConnection", cim.phaseConnection.name)

        regulatingCondEqToGeojson(this, cim)
    }

fun switchToGeojson(feature: Feature, cim: Switch): Feature =
    feature.apply {
        put("normallyOpen", cim.isNormallyOpen())
        put("open", cim.isOpen())
        // when unganged support is added
        // put("normallyOpen", cim.normalOpen)
        // put("open", cim.open)
        conductingEquipmentToGeojson(this, cim)
    }

fun tapChangerToGeojson(feature: Feature, cim: TapChanger): Feature =
    feature.apply {
        cim.highStep?.let { put("highStep", it) }
        cim.lowStep?.let { put("lowStep", it) }
        cim.step?.let { put("step", it) }
        cim.neutralStep?.let { put("neutralStep", it) }
        cim.neutralU?.let { put("neutralU", it) }
        cim.normalStep?.let { put("normalStep", it) }
        put("controlEnabled", cim.controlEnabled)

        powerSystemResourceToGeojson(this, cim)
    }

fun transformerEndToGeojson(feature: Feature, cim: TransformerEnd): Feature =
    feature.apply {
        cim.baseVoltage?.let { put("baseVoltageId", it.mRID) }
        cim.ratioTapChanger?.let { put("ratioTapChangerId", it.mRID) }
        cim.starImpedance?.let { put("starImpedanceId", it.mRID) }
        put("endNumber", cim.endNumber)
        put("grounded", cim.grounded)
        cim.rGround?.let { put("rGround", it) }
        cim.xGround?.let { put("xGround", it) }

        identifiedObjectToGeojson(this, cim)
    }

fun transformerStarImpedanceToGeojson(cim: TransformerStarImpedance): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.transformerEndInfo?.let { put("transformerEndInfoId", it.mRID) }
        cim.r?.let { put("r", it) }
        cim.r0?.let { put("r0", it) }
        cim.x?.let { put("x", it) }
        cim.x0?.let { put("x0", it) }

        identifiedObjectToGeojson(this, cim)
    }

/************ IEC61970 InfIEC61970 Feeder ************/

fun circuitToGeojson(cim: Circuit): Feature=
    Feature(cim.mRID, geometry = cim.location?.points?.let { positionPointsToGeometry(it) } ?: NULL_GEOMETRY).apply {
        cim.loop?.let { put("loopId", it.mRID) }

        cim.endTerminals.mapNotNull { it.conductingEquipment }.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("endEquipmentIds", it) }
        cim.endSubstations.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("endSubstationIds", it) }

        lineToGeojson(this, cim)
    }

fun loopToGeojson(cim: Loop): Feature =
    Feature(cim.mRID, geometry = NULL_GEOMETRY).apply {
        cim.circuits.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("circuitIds", it) }
        cim.substations.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("substationIds", it) }
        cim.energizingSubstations.map { JsonPrimitive(it.mRID) }.takeUnless { it.isEmpty() }?.let { put("energizingSubstationIds", it) }

        identifiedObjectToGeojson(this, cim)
    }
