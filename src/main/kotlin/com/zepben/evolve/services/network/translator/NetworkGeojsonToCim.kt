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
import com.zepben.evolve.cim.geojson.GeometryType
import com.zepben.evolve.cim.iec61968.assetinfo.*
import com.zepben.evolve.cim.iec61968.assets.*
import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.common.PositionPoint
import com.zepben.evolve.cim.iec61968.common.StreetAddress
import com.zepben.evolve.cim.iec61968.common.TownDetail
import com.zepben.evolve.cim.iec61968.metering.EndDevice
import com.zepben.evolve.cim.iec61968.metering.Meter
import com.zepben.evolve.cim.iec61968.metering.UsagePoint
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.AuxiliaryEquipment
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.FaultIndicator
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.domain.UnitSymbol
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentBranch
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentEquipment
import com.zepben.evolve.cim.iec61970.base.meas.*
import com.zepben.evolve.cim.iec61970.base.scada.RemoteControl
import com.zepben.evolve.cim.iec61970.base.scada.RemotePoint
import com.zepben.evolve.cim.iec61970.base.scada.RemoteSource
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.*
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Loop
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.UNKNOWN_INT
import com.zepben.evolve.services.common.extensions.internEmpty
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.common.translator.*
import com.zepben.evolve.services.network.NetworkService
import kotlinx.serialization.json.*


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
        energisedEndVoltage = feature.getInt("energisedEndVoltage")
        excitingCurrent = feature.getDouble("excitingCurrent")
        excitingCurrentZero = feature.getDouble("excitingCurrentZero")
        loss = feature.getInt("loss")
        lossZero = feature.getInt("lossZero")
        transformerTestToCim(feature, this, networkService)
    }

fun openCircuitTestToCim(feature: Feature, networkService: NetworkService): OpenCircuitTest =
    OpenCircuitTest(feature.id).apply {
        energisedEndStep = feature.getInt("energisedEndStep")
        energisedEndVoltage = feature.getInt("energisedEndVoltage")
        openEndStep = feature.getInt("openEndStep")
        openEndVoltage = feature.getInt("openEndVoltage")
        phaseShift = feature.getDouble("phaseShift")
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
        current = feature.getDouble("current")
        energisedEndStep = feature.getInt("energisedEndStep")
        groundedEndStep = feature.getInt("groundedEndStep")
        leakageImpedance = feature.getDouble("leakageImpedance")
        leakageImpedanceZero = feature.getDouble("leakageImpedanceZero")
        loss = feature.getInt("loss")
        lossZero = feature.getInt("lossZero")
        power = feature.getInt("power")
        voltage = feature.getDouble("voltage")
        voltageOhmicPart = feature.getDouble("voltageOhmicPart")
        transformerTestToCim(feature, this, networkService)
    }

fun transformerEndInfoToCim(feature: Feature, networkService: NetworkService): TransformerEndInfo =
    TransformerEndInfo(feature.id).apply {
        connectionKind = feature.getString("connectionKind")?.let { WindingConnection.valueOf(it) } ?: WindingConnection.UNKNOWN_WINDING
        emergencyS = feature.getInt("emergencyS")
        endNumber = feature.getInt("endNumber") ?: 0
        insulationU = feature.getInt("insulationU")
        phaseAngleClock = feature.getInt("phaseAngleClock")
        r = feature.getDouble("r")
        ratedS = feature.getInt("ratedS")
        ratedU = feature.getInt("ratedU")
        shortTermS = feature.getInt("shortTermS")

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
        basePower = feature.getInt("basePower")
        temperature = feature.getDouble("temperature")
        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun wireInfoToCim(feature: Feature, cim: WireInfo, networkService: NetworkService): WireInfo =
    cim.apply {
        ratedCurrent = feature.getInt("ratedCurrent").takeUnless { it == UNKNOWN_INT }
        material = feature.getString("material")?.let { WireMaterialKind.valueOf(it) } ?: WireMaterialKind.UNKNOWN
        assetInfoToCim(feature, this, networkService)
    }

/************ IEC61968 ASSETS ************/

/**
 * [Asset.location] stores the Geometry.
 */
fun assetToCim(feature: Feature, cim: Asset, networkService: NetworkService): Asset =
    cim.apply {
        location = feature.getMap("location")?.let { loc ->
            locationToCim(
                networkService,
                feature.id,
                coordinatesToCim(loc.getList("positionPoints")) { it.first() is JsonPrimitive } ?: geometryToCim(feature.geometry),
                streetAddressToCim(loc),
                loc.getString("id")
            )
        } ?: locationToCim(networkService, feature.id, geometryToCim(feature.geometry))

        feature.getStringList("organisationRoleIds")?.forEach {
            networkService.resolveOrDeferReference(Resolvers.organisationRoles(this), it)
        }
        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun assetContainerToCim(feature: Feature, cim: AssetContainer, networkService: NetworkService): AssetContainer =
    cim.apply { assetToCim(feature, this, networkService) }

fun assetInfoToCim(feature: Feature, cim: AssetInfo, networkService: NetworkService): AssetInfo =
    cim.apply { identifiedObjectToCim(feature.properties, this, networkService) }

fun assetOrganisationRoleToCim(feature: Feature, cim: AssetOrganisationRole, networkService: NetworkService): AssetOrganisationRole =
    cim.apply { organisationRoleToCim(feature, this, networkService) }

fun assetOwnerToCim(feature: Feature, networkService: NetworkService): AssetOwner =
    AssetOwner(feature.id).apply {
        assetOrganisationRoleToCim(feature, this, networkService)
    }

fun poleToCim(feature: Feature, networkService: NetworkService): Pole =
    Pole(feature.id).apply {
        classification = feature.getString("classification")?.internEmpty() ?: ""
        feature.getStringList("streetlightIds")?.forEach {
            networkService.resolveOrDeferReference(Resolvers.streetlights(this), it)
        }
        structureToCim(feature, this, networkService)
    }

fun streetlightToCim(feature: Feature, networkService: NetworkService): Streetlight =
    Streetlight(feature.id).apply {
        lampKind = feature.getString("lampKind")?.let { StreetlightLampKind.valueOf(it) } ?: StreetlightLampKind.UNKNOWN
        lightRating = feature.getInt("lightRating")
        networkService.resolveOrDeferReference(Resolvers.pole(this), feature.getString("poleId"))
        assetToCim(feature, this, networkService)
    }

fun structureToCim(feature: Feature, cim: Structure, networkService: NetworkService): Structure =
    cim.apply { assetContainerToCim(feature, this, networkService) }

/************ IEC61968 COMMON ************/

/**
 * Accepts two forms of JsonArray:
 *     "positionPoints": [ longitude, latitude ]
 *     "positionPoints": [ [ longitude, latitude ], [ longitude, latitude ], ..., [ longitude, latitude ] ]
 *
 * @return A list of PositionPoints, one for each longlat pair, or an empty list if none were specified.
 *
 * @throws NumberFormatException if primitives are not doubles.
 * @throws IllegalArgumentException if [Geometry.coordinates] is not a JsonArray of JsonArray's, or a JsonArray of JsonPrimitives
 */
fun geometryToCim(geometry: Geometry?): List<PositionPoint>? =
    geometry?.let { geo ->
        coordinatesToCim(geo.coordinates) { geo.type == GeometryType.Point }
    }


fun locationToCim(
    networkService: NetworkService,
    refererMRID: String,
    positionPoints: List<PositionPoint>? = null,
    streetAddress: StreetAddress? = null,
    id: String? = null
): Location? =
    if (positionPoints.isNullOrEmpty() && streetAddress == null && id == null)
        null
    else
        Location(if (id.isNullOrBlank()) "$refererMRID-loc" else id).apply {
            positionPoints?.forEach(::addPoint)
            mainAddress = streetAddress
            networkService.add(this)
        }

/**
 * Accepts two forms of JsonArray:
 *     "positionPoints": [ longitude, latitude ]
 *     "positionPoints": [ [ longitude, latitude ], [ longitude, latitude ], ..., [ longitude, latitude ] ]
 *
 * @return A list of PositionPoints, one for each longlat pair, or null if none were specified.
 *
 * @throws NumberFormatException if primitives are not doubles.
 * @throws IllegalArgumentException if it's not a JsonArray or a list of JsonPrimitives
 */
fun coordinatesToCim(coordinates: List<JsonElement>?, isPoint: (List<JsonElement>) -> Boolean): List<PositionPoint>? =
    coordinates?.takeUnless { it.isEmpty() }?.let { coords ->
        if (isPoint(coords))
            listOf(PositionPoint(coords[0].jsonPrimitive.double, coords[1].jsonPrimitive.double))
        else
            coords.map { array -> PositionPoint(array.jsonArray[0].jsonPrimitive.double, array.jsonArray[1].jsonPrimitive.double) }
    }

fun streetAddressToCim(mainAddress: Map<String, JsonElement>): StreetAddress =
    StreetAddress(mainAddress.getString("postalCode") ?: "", townDetailToCim(mainAddress))


fun townDetailToCim(mainAddress: Map<String, JsonElement>): TownDetail? {
    val n = mainAddress.getString("name") ?: ""
    val s = mainAddress.getString("stateOrProvince") ?: ""
    return if (n.isBlank() && s.isBlank())
        null
    else
        TownDetail(n, s)
}

/************ IEC61968 METERING ************/

fun endDeviceToCim(feature: Feature, cim: EndDevice, networkService: NetworkService): EndDevice =
    cim.apply {
        feature.getStringList("usagePointIds")?.forEach { usagePointMRID ->
            networkService.resolveOrDeferReference(Resolvers.usagePoints(this), usagePointMRID)
        }
        customerMRID = feature.getString("customerId")

        // Geometry will be captured by Asset.location if serviceLocation is not set.
        serviceLocation = feature.getMap("serviceLocation")?.let { sl ->
            locationToCim(
                networkService,
                "${feature.id}-service",
                coordinatesToCim(sl.getList("positionPoints")) { it.first() is JsonPrimitive } ?: geometryToCim(feature.geometry),
                streetAddressToCim(sl),
                sl.getString("id")
            )
        }

        assetContainerToCim(feature, this, networkService)
    }

fun meterToCim(feature: Feature, networkService: NetworkService): Meter =
    Meter(feature.id).apply {
        endDeviceToCim(feature, this, networkService)
    }

fun usagePointToCim(feature: Feature, networkService: NetworkService): UsagePoint =
    UsagePoint(feature.id).apply {
        usagePointLocation = feature.getMap("usagePointLocation")?.let { upl ->
            locationToCim(
                networkService,
                feature.id,
                coordinatesToCim(upl.getList("positionPoints")) { it.first() is JsonPrimitive } ?: geometryToCim(feature.geometry),
                streetAddressToCim(upl),
                upl.getString("id")
            )
        } ?: locationToCim(networkService, feature.id, geometryToCim(feature.geometry))

        feature.getStringList("equipmentIds")?.forEach { equipmentMRID ->
            networkService.resolveOrDeferReference(Resolvers.equipment(this), equipmentMRID)
        }

        feature.getStringList("endDeviceIds")?.forEach {
            networkService.resolveOrDeferReference(Resolvers.endDevices(this), it)
        }

        identifiedObjectToCim(feature.properties, this, networkService)
    }

/************ IEC61968 OPERATIONS ************/

fun operationalRestrictionToCim(feature: Feature, networkService: NetworkService): OperationalRestriction =
    OperationalRestriction(feature.id).apply {
        documentToCim(feature, this, networkService)
    }

/************ IEC61970 AUXILIARY EQUIPMENT ************/

fun auxiliaryEquipmentToCim(feature: Feature, cim: AuxiliaryEquipment, networkService: NetworkService): AuxiliaryEquipment =
    cim.apply {
        feature.getString("equipmentId1")?.let { eq1MRID ->
            feature.getString("equipmentId2")?.let { eq2MRID ->
                networkService.get<ConductingEquipment>(eq1MRID)?.let { eq1 ->
                    networkService.get<ConductingEquipment>(eq2MRID)?.let { eq2 ->
                        NetworkService.connectedEquipment(eq1).find {  cr -> cr.to == eq2 }?.let {
                            cim.terminal = it.fromTerminal
                        }
                    }
                }
            }
        }

        networkService.resolveOrDeferReference(Resolvers.terminal(this), feature.getString("terminalId"))
        equipmentToCim(feature, this, networkService)
    }

fun faultIndicatorToCim(feature: Feature, networkService: NetworkService): FaultIndicator =
    FaultIndicator(feature.id).apply {
        auxiliaryEquipmentToCim(feature, this, networkService)
    }

/************ IEC61970 CORE ************/

fun baseVoltageToCim(feature: Feature, networkService: NetworkService): BaseVoltage =
    BaseVoltage(feature.id).apply {
        nominalVoltage = feature.getInt("nominalVoltage") ?: 0
        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun conductingEquipmentToCim(feature: Feature, cim: ConductingEquipment, networkService: NetworkService): ConductingEquipment =
    cim.apply {
        // Note: Terminals are handled in conductorToCim
        networkService.resolveOrDeferReference(Resolvers.baseVoltage(this), feature.getString("baseVoltageId"))

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

        // Optional fields to indicate this equipment belongs to a Substation or Circuit
        networkService.resolveOrDeferReference(Resolvers.containers(this), feature.getString("substationId"))
        networkService.resolveOrDeferReference(Resolvers.containers(this), feature.getString("circuitId"))

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
        numControls = feature.getInt("numControls") ?: 0
        location = feature.getMap("location")?.let { loc ->
            locationToCim(
                networkService,
                feature.id,
                coordinatesToCim(loc.getList("positionPoints")) { it.first() is JsonPrimitive } ?: geometryToCim(feature.geometry),
                streetAddressToCim(loc),
                loc.getString("id")
            )
        } ?: locationToCim(networkService, feature.id, geometryToCim(feature.geometry))

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

/************ IEC61970 BASE EQUIVALENTS ************/
fun equivalentBranchToCim(feature: Feature, networkService: NetworkService): EquivalentBranch =
    EquivalentBranch(feature.id).apply {
        negativeR12 = feature.getDouble("negativeR12")
        negativeR21 = feature.getDouble("b.negativeR21")
        negativeX12 = feature.getDouble("negativeX12")
        negativeX21 = feature.getDouble("negativeX21")
        positiveR12 = feature.getDouble("positiveR12")
        positiveR21 = feature.getDouble("positiveR21")
        positiveX12 = feature.getDouble("positiveX12")
        positiveX21 = feature.getDouble("positiveX21")
        r = feature.getDouble("r")
        r21 = feature.getDouble("r21")
        x = feature.getDouble("x")
        x21 = feature.getDouble("x21")
        zeroR12 = feature.getDouble("zeroR12")
        zeroR21 = feature.getDouble("zeroR21")
        zeroX12 = feature.getDouble("zeroX12")
        zeroX21 = feature.getDouble("zeroX21")
        equivalentEquipmentToCim(feature, this, networkService)
    }

fun equivalentEquipmentToCim(feature: Feature, cim: EquivalentEquipment, networkService: NetworkService): EquivalentEquipment =
    cim.apply { conductingEquipmentToCim(feature, this, networkService) }

/************ IEC61970 MEAS ************/

fun controlToCim(feature: Feature, networkService: NetworkService): Control =
    Control(feature.id).apply {
        powerSystemResourceMRID = feature.getString("powerSystemResourceId")?.takeIf { it.isNotBlank() }
        networkService.resolveOrDeferReference(Resolvers.remoteControl(this), feature.getString("remoteControlId"))
        ioPointToCim(feature, this, networkService)
    }

fun ioPointToCim(feature: Feature, cim: IoPoint, networkService: NetworkService): IoPoint =
    cim.apply { identifiedObjectToCim(feature.properties, this, networkService) }

fun measurementToCim(feature: Feature, cim: Measurement, networkService: NetworkService) =
    cim.apply {
        powerSystemResourceMRID = feature.getString("powerSystemResourceId")?.takeIf { it.isNotBlank() }
        networkService.resolveOrDeferReference(Resolvers.remoteSource(this), feature.getString("remoteSourceId"))
        terminalMRID = feature.getString("terminalId")?.takeIf { it.isNotBlank() }
        phases = feature.getString("phases")?.let { PhaseCode.valueOf(it) } ?: PhaseCode.NONE
        unitSymbol = feature.getString("unitSymbol")?.let { UnitSymbol.valueOf(it) } ?: UnitSymbol.NONE
        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun accumulatorToCim(feature: Feature, networkService: NetworkService): Accumulator =
    Accumulator(feature.id).apply {
        measurementToCim(feature, this, networkService)
    }

fun analogToCim(feature: Feature, networkService: NetworkService): Analog =
    Analog(feature.id).apply {
        positiveFlowIn = feature.getBoolean("positiveFlowIn") ?: false
        measurementToCim(feature, this, networkService)
    }

fun discreteToCim(feature: Feature, networkService: NetworkService): Discrete =
    Discrete(feature.id).apply {
        measurementToCim(feature, this, networkService)
    }

/************ IEC61970 SCADA ************/

fun remoteControlToCim(feature: Feature, networkService: NetworkService): RemoteControl =
    RemoteControl(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.control(this), feature.getString("controlId"))
        remotePointToCim(feature, this, networkService)
    }

fun remotePointToCim(feature: Feature, cim: RemotePoint, networkService: NetworkService): RemotePoint =
    cim.apply { identifiedObjectToCim(feature.properties, this, networkService) }

fun remoteSourceToCim(feature: Feature, networkService: NetworkService): RemoteSource =
    RemoteSource(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.measurement(this), feature.getString("measurementId"))
        remotePointToCim(feature, this, networkService)
    }

/************ IEC61970 WIRES GENERATION PRODUCTION ************/

fun powerElectronicsUnitToCim(feature: Feature, cim: PowerElectronicsUnit, networkService: NetworkService): PowerElectronicsUnit =
    cim.apply {
        networkService.resolveOrDeferReference(Resolvers.powerElectronicsConnection(this), feature.getString("powerElectronicsConnectionId"))
        maxP = feature.getInt("maxP")
        minP = feature.getInt("minP")
        equipmentToCim(feature, this, networkService)
    }

fun batteryUnitToCim(feature: Feature, networkService: NetworkService): BatteryUnit =
    BatteryUnit(feature.id).apply {
        batteryState = feature.getString("batteryState")?.let { BatteryStateKind.valueOf(it) } ?: BatteryStateKind.UNKNOWN
        ratedE = feature.getLong("ratedE")
        storedE = feature.getLong("storedE")
        powerElectronicsUnitToCim(feature, this, networkService)
    }

fun photoVoltaicUnitToCim(feature: Feature, networkService: NetworkService): PhotoVoltaicUnit =
    PhotoVoltaicUnit(feature.id).apply {
        powerElectronicsUnitToCim(feature, this, networkService)
    }

fun powerElectronicsWindUnitToCim(feature: Feature, networkService: NetworkService): PowerElectronicsWindUnit =
    PowerElectronicsWindUnit(feature.id).apply {
        powerElectronicsUnitToCim(feature, this, networkService)
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

fun busbarSectionToCim(feature: Feature, networkService: NetworkService): BusbarSection =
    BusbarSection(feature.id).apply {
        connectorToCim(feature, this, networkService)
    }

fun conductorToCim(feature: Feature, cim: Conductor, networkService: NetworkService, connectivity: MutableList<Connectivity>): Conductor =
    cim.apply {
        length = feature.getDouble("length")
        networkService.resolveOrDeferReference(Resolvers.assetInfo(this), feature.getString("assetInfoId"))

        feature.getString("fromId")?.let { from ->
            feature.getString("toId")?.let { to ->
                connectivity.add(Connectivity(this, from, to, feature.getString("phases")?.let { PhaseCode.valueOf(it) } ?: PhaseCode.ABC))
            } ?: throw MissingPropertyException("toId")
        } ?: throw MissingPropertyException("fromId")

        conductingEquipmentToCim(feature, cim, networkService)
    }

fun connectorToCim(feature: Feature, cim: Connector, networkService: NetworkService): Connector =
    cim.apply { conductingEquipmentToCim(feature, this, networkService) }

fun disconnectorToCim(feature: Feature, networkService: NetworkService): Disconnector =
    Disconnector(feature.id).apply {
        switchToCim(feature, this, networkService)
    }

fun energyConnectionToCim(feature: Feature, cim: EnergyConnection, networkService: NetworkService): EnergyConnection =
    cim.apply { conductingEquipmentToCim(feature, cim, networkService) }

fun energyConsumerToCim(feature: Feature, networkService: NetworkService): EnergyConsumer =
    EnergyConsumer(feature.id).apply {

        feature.getStringList("energyConsumerPhaseIds")?.forEach { energyConsumerPhasesMRID ->
            networkService.resolveOrDeferReference(Resolvers.phases(this), energyConsumerPhasesMRID)
        }
        customerCount = feature.getInt("customerCount")
        grounded = feature.getBoolean("grounded") ?: false
        p = feature.getDouble("p")
        pFixed = feature.getDouble("pFixed")
        phaseConnection = feature.getString("phaseConnection")?.let { PhaseShuntConnectionKind.valueOf(it) } ?: PhaseShuntConnectionKind.UNKNOWN
        q = feature.getDouble("q")
        qFixed = feature.getDouble("qFixed")
        energyConnectionToCim(feature, this, networkService)
    }

fun energyConsumerPhaseToCim(feature: Feature, networkService: NetworkService): EnergyConsumerPhase =
    EnergyConsumerPhase(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.energyConsumer(this), feature.getString("energyConsumerId"))
        phase = feature.getString("phase")?.let { SinglePhaseKind.valueOf(it) } ?: SinglePhaseKind.NONE
        p = feature.getDouble("p")
        pFixed = feature.getDouble("pFixed")
        q = feature.getDouble("q")
        qFixed = feature.getDouble("qFixed")
        powerSystemResourceToCim(feature, this, networkService)
    }

fun energySourceToCim(feature: Feature, networkService: NetworkService): EnergySource =
    EnergySource(feature.id).apply {
        feature.getStringList("energySourcePhaseIds")?.forEach { energySourcePhasesMRID ->
            networkService.resolveOrDeferReference(Resolvers.phases(this), energySourcePhasesMRID)
        }
        activePower = feature.getDouble("activePower")
        reactivePower = feature.getDouble("reactivePower")
        voltageAngle = feature.getDouble("voltageAngle")
        voltageMagnitude = feature.getDouble("voltageMagnitude")
        r = feature.getDouble("r")
        x = feature.getDouble("x")
        pMax = feature.getDouble("pMax")
        pMin = feature.getDouble("pMin")
        r0 = feature.getDouble("r0")
        rn = feature.getDouble("rn")
        x0 = feature.getDouble("x0")
        xn = feature.getDouble("xn")
        energyConnectionToCim(feature, this, networkService)
    }

fun energySourcePhaseToCim(feature: Feature, networkService: NetworkService): EnergySourcePhase =
    EnergySourcePhase(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.energySource(this), feature.getString("energySourceId"))
        phase = feature.getString("phase")?.let { SinglePhaseKind.valueOf(it) } ?: SinglePhaseKind.NONE
        powerSystemResourceToCim(feature, this, networkService)
    }

fun fuseToCim(feature: Feature, networkService: NetworkService): Fuse =
    Fuse(feature.id).apply {
        switchToCim(feature, this, networkService)
    }

fun jumperToCim(feature: Feature, networkService: NetworkService): Jumper =
    Jumper(feature.id).apply {
        switchToCim(feature, this, networkService)
    }

fun junctionToCim(feature: Feature, networkService: NetworkService): Junction =
    Junction(feature.id).apply {
        connectorToCim(feature, this, networkService)
    }

fun lineToCim(feature: Feature, cim: Line, networkService: NetworkService): Line =
    cim.apply { equipmentContainerToCim(feature, this, networkService) }

fun linearShuntCompensatorToCim(feature: Feature, networkService: NetworkService): LinearShuntCompensator =
    LinearShuntCompensator(feature.id).apply {
        b0PerSection = feature.getDouble("b0PerSection")
        bPerSection = feature.getDouble("bPerSection")
        g0PerSection = feature.getDouble("g0PerSection")
        gPerSection = feature.getDouble("gPerSection")
        shuntCompensatorToCim(feature, this, networkService)
    }

fun loadBreakSwitchToCim(feature: Feature, networkService: NetworkService): LoadBreakSwitch =
    LoadBreakSwitch(feature.id).apply {
        protectedSwitchToCim(feature, this, networkService)
    }

fun perLengthLineParameterToCim(feature: Feature, cim: PerLengthLineParameter, networkService: NetworkService): PerLengthLineParameter =
    cim.apply { identifiedObjectToCim(feature.properties, this, networkService) }

fun perLengthImpedanceToCim(feature: Feature, cim: PerLengthImpedance, networkService: NetworkService): PerLengthImpedance =
    cim.apply { perLengthLineParameterToCim(feature, cim, networkService) }

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

fun powerElectronicsConnectionToCim(feature: Feature, networkService: NetworkService): PowerElectronicsConnection =
    PowerElectronicsConnection(feature.id).apply {
        feature.getStringList("powerElectronicsUnitIds")?.forEach { powerElectronicsUnitMRID ->
            networkService.resolveOrDeferReference(Resolvers.powerElectronicsUnit(this), powerElectronicsUnitMRID)
        }
        feature.getStringList("powerElectronicsConnectionPhaseIds")?.forEach { powerElectronicsConnectionPhaseMRID ->
            networkService.resolveOrDeferReference(Resolvers.powerElectronicsConnectionPhase(this), powerElectronicsConnectionPhaseMRID)
        }
        maxIFault = feature.getInt("maxIFault")
        maxQ = feature.getDouble("maxQ")
        minQ = feature.getDouble("minQ")
        p = feature.getDouble("p")
        q = feature.getDouble("q")
        ratedS = feature.getInt("ratedS")
        ratedU = feature.getInt("ratedU")
        regulatingCondEqToCim(feature, this, networkService)
    }

fun powerElectronicsConnectionPhaseToCim(feature: Feature, networkService: NetworkService): PowerElectronicsConnectionPhase =
    PowerElectronicsConnectionPhase(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.powerElectronicsConnection(this), feature.getString("powerElectronicsConnectionId"))
        p = feature.getDouble("p")
        phase = feature.getString("phase")?.let { SinglePhaseKind.valueOf(it) } ?: SinglePhaseKind.NONE
        q = feature.getDouble("q")
        powerSystemResourceToCim(feature, this, networkService)
    }

fun powerTransformerToCim(feature: Feature, networkService: NetworkService): PowerTransformer =
    PowerTransformer(feature.id).apply {
        feature.getStringList("powerTransformerEndIds")?.forEach { endMRID ->
            networkService.resolveOrDeferReference(Resolvers.ends(this), endMRID)
        }

        vectorGroup = feature.getString("vectorGroup")?.let { VectorGroup.valueOf(it) } ?: VectorGroup.UNKNOWN
        transformerUtilisation = feature.getDouble("transformerUtilisation")
        networkService.resolveOrDeferReference(Resolvers.assetInfo(this), feature.getString("assetInfoId"))

        conductingEquipmentToCim(feature, this, networkService)
    }

fun powerTransformerEndToCim(feature: Feature, networkService: NetworkService): PowerTransformerEnd =
    PowerTransformerEnd(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.powerTransformer(this), feature.getString("powerTransformerId"))
        ratedS = feature.getInt("ratedS")
        ratedU = feature.getInt("ratedU")
        r = feature.getDouble("r")
        r0 = feature.getDouble("r0")
        x = feature.getDouble("x")
        x0 = feature.getDouble("x0")
        connectionKind = feature.getString("connectionKind")?.let { WindingConnection.valueOf(it) } ?: WindingConnection.UNKNOWN_WINDING
        b = feature.getDouble("b")
        b0 = feature.getDouble("b0")
        g = feature.getDouble("g")
        g0 = feature.getDouble("g0")
        phaseAngleClock = feature.getInt("phaseAngleClock")

        transformerEndToCim(feature, this, networkService)
    }

fun protectedSwitchToCim(feature: Feature, cim: ProtectedSwitch, networkService: NetworkService): ProtectedSwitch =
    cim.apply { switchToCim(feature, cim, networkService) }

fun ratioTapChangerToCim(feature: Feature, networkService: NetworkService): RatioTapChanger =
    RatioTapChanger(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.transformerEnd(this), feature.getString("transformerEndId"))
        stepVoltageIncrement = feature.getDouble("stepVoltageIncrement")
        tapChangerToCim(feature, this, networkService)
    }

fun recloserToCim(feature: Feature, networkService: NetworkService): Recloser =
    Recloser(feature.id).apply { protectedSwitchToCim(feature, this, networkService) }

fun regulatingCondEqToCim(feature: Feature, cim: RegulatingCondEq, networkService: NetworkService): RegulatingCondEq =
    cim.apply {
        controlEnabled = feature.getBoolean("controlEnabled") ?: true
        energyConnectionToCim(feature, this, networkService)
    }

fun shuntCompensatorToCim(feature: Feature, cim: ShuntCompensator, networkService: NetworkService): ShuntCompensator =
    cim.apply {
        sections = feature.getDouble("sections")
        grounded = feature.getBoolean("grounded") ?: false
        nomU = feature.getInt("nomU")
        phaseConnection = feature.getString("phaseConnection")?.let { PhaseShuntConnectionKind.valueOf(it) } ?: PhaseShuntConnectionKind.UNKNOWN
        regulatingCondEqToCim(feature, this, networkService)
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

fun tapChangerToCim(feature: Feature, cim: TapChanger, networkService: NetworkService): TapChanger =
    cim.apply {
        highStep = feature.getInt("highStep")
        lowStep = feature.getInt("lowStep")
        step = feature.getDouble("step")
        neutralStep = feature.getInt("neutralStep")
        neutralU = feature.getInt("neutralU")
        normalStep = feature.getInt("normalStep")
        controlEnabled = feature.getBoolean("controlEnabled") ?: true
        powerSystemResourceToCim(feature, this, networkService)
    }

fun transformerEndToCim(feature: Feature, cim: TransformerEnd, networkService: NetworkService): TransformerEnd =
    cim.apply {
        // TODO In GeoJSON transformer ends are ConductingEquipment - not PowerTransformers. Terminal must be populated in ConnectivityNode
//        networkService.resolveOrDeferReference(Resolvers.terminal(this), feature.getString("terminalId"))
        networkService.resolveOrDeferReference(Resolvers.baseVoltage(this), feature.getString("baseVoltageId"))
        networkService.resolveOrDeferReference(Resolvers.ratioTapChanger(this), feature.getString("ratioTapChangerId"))
        networkService.resolveOrDeferReference(Resolvers.starImpedance(this), feature.getString("starImpedanceId"))
        endNumber = feature.getInt("endNumber") ?: 0
        grounded = feature.getBoolean("grounded") ?: false
        rGround = feature.getDouble("rGround")
        xGround = feature.getDouble("xGround")
        identifiedObjectToCim(feature.properties, this, networkService)
    }

fun transformerStarImpedanceToCim(feature: Feature, networkService: NetworkService): TransformerStarImpedance =
    TransformerStarImpedance(feature.id).apply {
        networkService.resolveOrDeferReference(Resolvers.transformerEndInfo(this), feature.getString("transformerEndInfoId"))
        r = feature.getDouble("r")
        r0 = feature.getDouble("r0")
        x = feature.getDouble("x")
        x0 = feature.getDouble("x0")
        identifiedObjectToCim(feature.properties, this, networkService)
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
        feature.getStringList("circuitIds")?.forEach { circuitMRID ->
            networkService.resolveOrDeferReference(Resolvers.circuits(this), circuitMRID)
        }

        feature.getStringList("substationIds")?.forEach { substationMRID ->
            networkService.resolveOrDeferReference(Resolvers.substations(this), substationMRID)
        }

        feature.getStringList("energizingSubstationIds")?.forEach { normalEnergizingSubstationMRID ->
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

// TODO: this needs a java interface, and maybe renaming/relocating - i haven't thought about it much.
fun convertGeojsonToCim(featureCollection: FeatureCollection, networkService: NetworkService) {
    // TODO remove and make reliant on ordering - requires some changes to the effected ToCim functions.
    val connectivity = mutableListOf<Connectivity>()
    val headEquipment = mutableMapOf<String, String>()
    val endEquipment = mutableMapOf<String, MutableList<String>>()

    // TODO this could probably be made more memory efficient if we split it into two parts so we don't hold onto the FeatureCollection after the classMap is built.
    // It probably doesn't matter though assuming this function is always called feeder at a time.
    val classMap = mutableMapOf<String, MutableList<Feature>>()
    featureCollection.features.forEach { feature ->
        val clazz = feature.getString("class") ?: throw MissingPropertyException("Feature ${feature.id} missing required property 'class'")
        classMap.computeIfAbsent(clazz) { mutableListOf() }.add(feature)
    }

    // Order here matters. Typically must be in the same order as NetworkServiceReader
    networkService.apply {
        // IEC61968 ASSETINFO
        classMap["CableInfo"]?.forEach { feature -> tryAddOrNull(cableInfoToCim(feature, this)) }
        classMap["NoLoadTest"]?.forEach { feature -> tryAddOrNull(noLoadTestToCim(feature, this)) }
        classMap["OpenCircuitTest"]?.forEach { feature -> tryAddOrNull(openCircuitTestToCim(feature, this)) }
        classMap["OverheadWireInfo"]?.forEach { feature -> tryAddOrNull(overheadWireInfoToCim(feature, this)) }
        classMap["PowerTransformerInfo"]?.forEach { feature -> tryAddOrNull(powerTransformerInfoToCim(feature, this)) }
        classMap["ShortCircuitTest"]?.forEach { feature -> tryAddOrNull(shortCircuitTestToCim(feature, this)) }
        classMap["TransformerEndInfo"]?.forEach { feature -> tryAddOrNull(transformerEndInfoToCim(feature, this)) }
        classMap["TransformerTankInfo"]?.forEach { feature -> tryAddOrNull(transformerTankInfoToCim(feature, this)) }

        // IEC61968 COMMON
        classMap["Organisation"]?.forEach { feature -> tryAddOrNull(organisationToCim(feature, this)) }

        // IEC61968 ASSETS
        classMap["AssetOwner"]?.forEach { feature -> tryAddOrNull(assetOwnerToCim(feature, this)) }
        classMap["Pole"]?.forEach { feature -> tryAddOrNull(poleToCim(feature, this)) }
        classMap["Streetlight"]?.forEach { feature -> tryAddOrNull(streetlightToCim(feature, this)) }

        // IEC61968 METERING
        classMap["Meter"]?.forEach { feature -> tryAddOrNull(meterToCim(feature, this)) }
        classMap["UsagePoint"]?.forEach { feature -> tryAddOrNull(usagePointToCim(feature, this)) }

        // IEC61968 OPERATIONS
        classMap["OperationalRestriction"]?.forEach { feature -> tryAddOrNull(operationalRestrictionToCim(feature, this)) }

        // IEC61970 BASE CORE
        classMap["BaseVoltage"]?.forEach { feature -> tryAddOrNull(baseVoltageToCim(feature, this)) }
        classMap["GeographicalRegion"]?.forEach { feature -> tryAddOrNull(geographicalRegionToCim(feature, this)) }
        classMap["NameType"]?.forEach { feature -> nameTypeToCim(feature, this) } // special case
        classMap["SubGeographicalRegion"]?.forEach { feature -> tryAddOrNull(subGeographicalRegionToCim(feature, this)) }
        classMap["Substation"]?.forEach { feature -> tryAddOrNull(substationToCim(feature, this)) }
        classMap["Site"]?.forEach { feature -> tryAddOrNull(siteToCim(feature, this)) }

        classMap["PerLengthSequenceImpedance"]?.forEach { feature -> tryAddOrNull(perLengthSequenceImpedanceToCim(feature, this)) }

        // IEC61970 BASE BASE EQUIVALENTS
        classMap["EquivalentBranch"]?.forEach { feature -> tryAddOrNull(equivalentBranchToCim(feature, this)) }

        // IEC61970 BASE WIRES
        classMap["PowerElectronicsConnection"]?.forEach { feature -> tryAddOrNull(powerElectronicsConnectionToCim(feature, this)) }
        classMap["PowerElectronicsConnectionPhase"]?.forEach { feature -> tryAddOrNull(powerElectronicsConnectionPhaseToCim(feature, this)) }

        // IEC61970 BASE WIRES GENERATION PRODUCTION
        classMap["BatteryUnit"]?.forEach { feature -> tryAddOrNull(batteryUnitToCim(feature, this)) }
        classMap["PhotoVoltaicUnit"]?.forEach { feature -> tryAddOrNull(photoVoltaicUnitToCim(feature, this)) }
        classMap["PowerElectronicsWindUnit"]?.forEach { feature -> tryAddOrNull(powerElectronicsWindUnitToCim(feature, this)) }

        // IEC61970 BASE WIRES
        classMap["AcLineSegment"]?.forEach { feature -> tryAddOrNull(acLineSegmentToCim(feature, this, connectivity)) }
        classMap["Breaker"]?.forEach { feature -> tryAddOrNull(breakerToCim(feature, this)) }
        classMap["BusbarSection"]?.forEach { feature -> tryAddOrNull(busbarSectionToCim(feature, this)) }
        classMap["Disconnector"]?.forEach { feature -> tryAddOrNull(disconnectorToCim(feature, this)) }
        classMap["EnergyConsumer"]?.forEach { feature -> tryAddOrNull(energyConsumerToCim(feature, this)) }
        classMap["EnergyConsumerPhase"]?.forEach { feature -> tryAddOrNull(energyConsumerPhaseToCim(feature, this)) }
        classMap["EnergySource"]?.forEach { feature -> tryAddOrNull(energySourceToCim(feature, this)) }
        classMap["EnergySourcePhase"]?.forEach { feature -> tryAddOrNull(energySourcePhaseToCim(feature, this)) }
        classMap["Fuse"]?.forEach { feature -> tryAddOrNull(fuseToCim(feature, this)) }
        classMap["Jumper"]?.forEach { feature -> tryAddOrNull(jumperToCim(feature, this)) }
        classMap["Junction"]?.forEach { feature -> tryAddOrNull(junctionToCim(feature, this)) }
        classMap["LinearShuntCompensator"]?.forEach { feature -> tryAddOrNull(linearShuntCompensatorToCim(feature, this)) }
        classMap["LoadBreakSwitch"]?.forEach { feature -> tryAddOrNull(loadBreakSwitchToCim(feature, this)) }
        classMap["PowerTransformer"]?.forEach { feature -> tryAddOrNull(powerTransformerToCim(feature, this)) }
        classMap["PowerTransformerEnd"]?.forEach { feature -> tryAddOrNull(powerTransformerEndToCim(feature, this)) }
        classMap["RatioTapChanger"]?.forEach { feature -> tryAddOrNull(ratioTapChangerToCim(feature, this)) }
        classMap["Recloser"]?.forEach { feature -> tryAddOrNull(recloserToCim(feature, this)) }
        classMap["TransformerStarImpedance"]?.forEach { feature -> tryAddOrNull(transformerStarImpedanceToCim(feature, this)) }

        // IEC61970 BASE AUXILIARY EQUIPMENT
        classMap["FaultIndicator"]?.forEach { feature -> tryAddOrNull(faultIndicatorToCim(feature, this)) }

        // IEC61970 BASE CORE
        classMap["Feeder"]?.forEach { feature -> tryAddOrNull(feederToCim(feature, this, headEquipment)) }

        // IEC61970 InfIEC61970 Feeder
        classMap["Circuit"]?.forEach { feature -> tryAddOrNull(circuitToCim(feature, this, endEquipment)) }
        classMap["Loop"]?.forEach { feature -> tryAddOrNull(loopToCim(feature, this)) }

        // IEC61970 BASE MEAS
        classMap["Analog"]?.forEach { feature -> tryAddOrNull(analogToCim(feature, this)) }
        classMap["Accumulator"]?.forEach { feature -> tryAddOrNull(accumulatorToCim(feature, this)) }
        classMap["Control"]?.forEach { feature -> tryAddOrNull(controlToCim(feature, this)) }
        classMap["Discrete"]?.forEach { feature -> tryAddOrNull(discreteToCim(feature, this)) }

        // IEC61970 BASE SCADA
        classMap["RemoteControl"]?.forEach { feature -> tryAddOrNull(remoteControlToCim(feature, this)) }
        classMap["RemoteSource"]?.forEach { feature -> tryAddOrNull(remoteSourceToCim(feature, this)) }


        // TODO remove
        createAndConnectTerminals(this, connectivity)
        connectFeederTerminals(this, headEquipment)
        connectCircuitTerminals(this, endEquipment)
    }

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