/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network.translator

import com.zepben.evolve.cim.iec61968.assetinfo.*
import com.zepben.evolve.cim.iec61968.assets.*
import com.zepben.evolve.cim.iec61968.common.*
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
import com.zepben.evolve.services.common.*
import com.zepben.evolve.services.common.extensions.internEmpty
import com.zepben.evolve.services.common.translator.BaseProtoToCim
import com.zepben.evolve.services.common.translator.toCim
import com.zepben.evolve.services.network.NetworkService
import com.zepben.protobuf.cim.iec61968.assetinfo.CableInfo as PBCableInfo
import com.zepben.protobuf.cim.iec61968.assetinfo.NoLoadTest as PBNoLoadTest
import com.zepben.protobuf.cim.iec61968.assetinfo.OpenCircuitTest as PBOpenCircuitTest
import com.zepben.protobuf.cim.iec61968.assetinfo.OverheadWireInfo as PBOverheadWireInfo
import com.zepben.protobuf.cim.iec61968.assetinfo.PowerTransformerInfo as PBPowerTransformerInfo
import com.zepben.protobuf.cim.iec61968.assetinfo.ShortCircuitTest as PBShortCircuitTest
import com.zepben.protobuf.cim.iec61968.assetinfo.TransformerEndInfo as PBTransformerEndInfo
import com.zepben.protobuf.cim.iec61968.assetinfo.TransformerTankInfo as PBTransformerTankInfo
import com.zepben.protobuf.cim.iec61968.assetinfo.TransformerTest as PBTransformerTest
import com.zepben.protobuf.cim.iec61968.assetinfo.WireInfo as PBWireInfo
import com.zepben.protobuf.cim.iec61968.assets.Asset as PBAsset
import com.zepben.protobuf.cim.iec61968.assets.AssetContainer as PBAssetContainer
import com.zepben.protobuf.cim.iec61968.assets.AssetInfo as PBAssetInfo
import com.zepben.protobuf.cim.iec61968.assets.AssetOrganisationRole as PBAssetOrganisationRole
import com.zepben.protobuf.cim.iec61968.assets.AssetOwner as PBAssetOwner
import com.zepben.protobuf.cim.iec61968.assets.Pole as PBPole
import com.zepben.protobuf.cim.iec61968.assets.Streetlight as PBStreetlight
import com.zepben.protobuf.cim.iec61968.assets.Structure as PBStructure
import com.zepben.protobuf.cim.iec61968.common.Location as PBLocation
import com.zepben.protobuf.cim.iec61968.common.Organisation as PBOrganisation
import com.zepben.protobuf.cim.iec61968.common.PositionPoint as PBPositionPoint
import com.zepben.protobuf.cim.iec61968.common.StreetAddress as PBStreetAddress
import com.zepben.protobuf.cim.iec61968.common.TownDetail as PBTownDetail
import com.zepben.protobuf.cim.iec61968.metering.EndDevice as PBEndDevice
import com.zepben.protobuf.cim.iec61968.metering.Meter as PBMeter
import com.zepben.protobuf.cim.iec61968.metering.UsagePoint as PBUsagePoint
import com.zepben.protobuf.cim.iec61968.operations.OperationalRestriction as PBOperationalRestriction
import com.zepben.protobuf.cim.iec61970.base.auxiliaryequipment.AuxiliaryEquipment as PBAuxiliaryEquipment
import com.zepben.protobuf.cim.iec61970.base.auxiliaryequipment.FaultIndicator as PBFaultIndicator
import com.zepben.protobuf.cim.iec61970.base.core.AcDcTerminal as PBAcDcTerminal
import com.zepben.protobuf.cim.iec61970.base.core.BaseVoltage as PBBaseVoltage
import com.zepben.protobuf.cim.iec61970.base.core.ConductingEquipment as PBConductingEquipment
import com.zepben.protobuf.cim.iec61970.base.core.ConnectivityNode as PBConnectivityNode
import com.zepben.protobuf.cim.iec61970.base.core.ConnectivityNodeContainer as PBConnectivityNodeContainer
import com.zepben.protobuf.cim.iec61970.base.core.Equipment as PBEquipment
import com.zepben.protobuf.cim.iec61970.base.core.EquipmentContainer as PBEquipmentContainer
import com.zepben.protobuf.cim.iec61970.base.core.Feeder as PBFeeder
import com.zepben.protobuf.cim.iec61970.base.core.GeographicalRegion as PBGeographicalRegion
import com.zepben.protobuf.cim.iec61970.base.core.NameType as PBNameType
import com.zepben.protobuf.cim.iec61970.base.core.PowerSystemResource as PBPowerSystemResource
import com.zepben.protobuf.cim.iec61970.base.core.Site as PBSite
import com.zepben.protobuf.cim.iec61970.base.core.SubGeographicalRegion as PBSubGeographicalRegion
import com.zepben.protobuf.cim.iec61970.base.core.Substation as PBSubstation
import com.zepben.protobuf.cim.iec61970.base.core.Terminal as PBTerminal
import com.zepben.protobuf.cim.iec61970.base.equivalents.EquivalentBranch as PBEquivalentBranch
import com.zepben.protobuf.cim.iec61970.base.equivalents.EquivalentEquipment as PBEquivalentEquipment
import com.zepben.protobuf.cim.iec61970.base.meas.Accumulator as PBAccumulator
import com.zepben.protobuf.cim.iec61970.base.meas.Analog as PBAnalog
import com.zepben.protobuf.cim.iec61970.base.meas.Control as PBControl
import com.zepben.protobuf.cim.iec61970.base.meas.Discrete as PBDiscrete
import com.zepben.protobuf.cim.iec61970.base.meas.IoPoint as PBIoPoint
import com.zepben.protobuf.cim.iec61970.base.meas.Measurement as PBMeasurement
import com.zepben.protobuf.cim.iec61970.base.scada.RemoteControl as PBRemoteControl
import com.zepben.protobuf.cim.iec61970.base.scada.RemotePoint as PBRemotePoint
import com.zepben.protobuf.cim.iec61970.base.scada.RemoteSource as PBRemoteSource
import com.zepben.protobuf.cim.iec61970.base.wires.AcLineSegment as PBAcLineSegment
import com.zepben.protobuf.cim.iec61970.base.wires.Breaker as PBBreaker
import com.zepben.protobuf.cim.iec61970.base.wires.BusbarSection as PBBusbarSection
import com.zepben.protobuf.cim.iec61970.base.wires.Conductor as PBConductor
import com.zepben.protobuf.cim.iec61970.base.wires.Connector as PBConnector
import com.zepben.protobuf.cim.iec61970.base.wires.Disconnector as PBDisconnector
import com.zepben.protobuf.cim.iec61970.base.wires.EnergyConnection as PBEnergyConnection
import com.zepben.protobuf.cim.iec61970.base.wires.EnergyConsumer as PBEnergyConsumer
import com.zepben.protobuf.cim.iec61970.base.wires.EnergyConsumerPhase as PBEnergyConsumerPhase
import com.zepben.protobuf.cim.iec61970.base.wires.EnergySource as PBEnergySource
import com.zepben.protobuf.cim.iec61970.base.wires.EnergySourcePhase as PBEnergySourcePhase
import com.zepben.protobuf.cim.iec61970.base.wires.Fuse as PBFuse
import com.zepben.protobuf.cim.iec61970.base.wires.Jumper as PBJumper
import com.zepben.protobuf.cim.iec61970.base.wires.Junction as PBJunction
import com.zepben.protobuf.cim.iec61970.base.wires.Line as PBLine
import com.zepben.protobuf.cim.iec61970.base.wires.LinearShuntCompensator as PBLinearShuntCompensator
import com.zepben.protobuf.cim.iec61970.base.wires.LoadBreakSwitch as PBLoadBreakSwitch
import com.zepben.protobuf.cim.iec61970.base.wires.PerLengthImpedance as PBPerLengthImpedance
import com.zepben.protobuf.cim.iec61970.base.wires.PerLengthLineParameter as PBPerLengthLineParameter
import com.zepben.protobuf.cim.iec61970.base.wires.PerLengthSequenceImpedance as PBPerLengthSequenceImpedance
import com.zepben.protobuf.cim.iec61970.base.wires.PowerElectronicsConnection as PBPowerElectronicsConnection
import com.zepben.protobuf.cim.iec61970.base.wires.PowerElectronicsConnectionPhase as PBPowerElectronicsConnectionPhase
import com.zepben.protobuf.cim.iec61970.base.wires.PowerTransformer as PBPowerTransformer
import com.zepben.protobuf.cim.iec61970.base.wires.PowerTransformerEnd as PBPowerTransformerEnd
import com.zepben.protobuf.cim.iec61970.base.wires.ProtectedSwitch as PBProtectedSwitch
import com.zepben.protobuf.cim.iec61970.base.wires.RatioTapChanger as PBRatioTapChanger
import com.zepben.protobuf.cim.iec61970.base.wires.Recloser as PBRecloser
import com.zepben.protobuf.cim.iec61970.base.wires.RegulatingCondEq as PBRegulatingCondEq
import com.zepben.protobuf.cim.iec61970.base.wires.ShuntCompensator as PBShuntCompensator
import com.zepben.protobuf.cim.iec61970.base.wires.Switch as PBSwitch
import com.zepben.protobuf.cim.iec61970.base.wires.TapChanger as PBTapChanger
import com.zepben.protobuf.cim.iec61970.base.wires.TransformerEnd as PBTransformerEnd
import com.zepben.protobuf.cim.iec61970.base.wires.TransformerStarImpedance as PBTransformerStarImpedance
import com.zepben.protobuf.cim.iec61970.base.wires.generation.production.BatteryUnit as PBBatteryUnit
import com.zepben.protobuf.cim.iec61970.base.wires.generation.production.PhotoVoltaicUnit as PBPhotoVoltaicUnit
import com.zepben.protobuf.cim.iec61970.base.wires.generation.production.PowerElectronicsUnit as PBPowerElectronicsUnit
import com.zepben.protobuf.cim.iec61970.base.wires.generation.production.PowerElectronicsWindUnit as PBPowerElectronicsWindUnit
import com.zepben.protobuf.cim.iec61970.infiec61970.feeder.Circuit as PBCircuit
import com.zepben.protobuf.cim.iec61970.infiec61970.feeder.Loop as PBLoop

/************ IEC61968 ASSET INFO ************/

fun toCim(pb: PBCableInfo, networkService: NetworkService): CableInfo =
    CableInfo(pb.mRID()).apply {
        toCim(pb.wi, this, networkService)
    }

fun toCim(pb: PBOverheadWireInfo, networkService: NetworkService): OverheadWireInfo =
    OverheadWireInfo(pb.mRID()).apply {
        toCim(pb.wi, this, networkService)
    }

fun toCim(pb: PBNoLoadTest, networkService: NetworkService): NoLoadTest =
    NoLoadTest(pb.mRID()).apply {
        energisedEndVoltage = pb.energisedEndVoltage.takeUnless { it == UNKNOWN_INT }
        excitingCurrent = pb.excitingCurrent.takeUnless { it == UNKNOWN_DOUBLE }
        excitingCurrentZero = pb.excitingCurrentZero.takeUnless { it == UNKNOWN_DOUBLE }
        loss = pb.loss.takeUnless { it == UNKNOWN_INT }
        lossZero = pb.lossZero.takeUnless { it == UNKNOWN_INT }
        toCim(pb.tt, this, networkService)
    }

fun toCim(pb: PBOpenCircuitTest, networkService: NetworkService): OpenCircuitTest =
    OpenCircuitTest(pb.mRID()).apply {
        energisedEndStep = pb.energisedEndStep.takeUnless { it == UNKNOWN_INT }
        energisedEndVoltage = pb.energisedEndVoltage.takeUnless { it == UNKNOWN_INT }
        openEndStep = pb.openEndStep.takeUnless { it == UNKNOWN_INT }
        openEndVoltage = pb.openEndVoltage.takeUnless { it == UNKNOWN_INT }
        phaseShift = pb.phaseShift.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.tt, this, networkService)
    }

fun toCim(pb: PBPowerTransformerInfo, networkService: NetworkService): PowerTransformerInfo =
    PowerTransformerInfo(pb.mRID()).apply {
        pb.transformerTankInfoMRIDsList.forEach {
            networkService.resolveOrDeferReference(Resolvers.transformerTankInfo(this), it)
        }
        toCim(pb.ai, this, networkService)
    }

fun toCim(pb: PBShortCircuitTest, networkService: NetworkService): ShortCircuitTest =
    ShortCircuitTest(pb.mRID()).apply {
        current = pb.current.takeUnless { it == UNKNOWN_DOUBLE }
        energisedEndStep = pb.energisedEndStep.takeUnless { it == UNKNOWN_INT }
        groundedEndStep = pb.groundedEndStep.takeUnless { it == UNKNOWN_INT }
        leakageImpedance = pb.leakageImpedance.takeUnless { it == UNKNOWN_DOUBLE }
        leakageImpedanceZero = pb.leakageImpedanceZero.takeUnless { it == UNKNOWN_DOUBLE }
        loss = pb.loss.takeUnless { it == UNKNOWN_INT }
        lossZero = pb.lossZero.takeUnless { it == UNKNOWN_INT }
        power = pb.power.takeUnless { it == UNKNOWN_INT }
        voltage = pb.voltage.takeUnless { it == UNKNOWN_DOUBLE }
        voltageOhmicPart = pb.voltageOhmicPart.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.tt, this, networkService)
    }

fun toCim(pb: PBTransformerEndInfo, networkService: NetworkService): TransformerEndInfo =
    TransformerEndInfo(pb.mRID()).apply {
        connectionKind = WindingConnection.valueOf(pb.connectionKind.name)
        emergencyS = pb.emergencyS.takeUnless { it == UNKNOWN_INT }
        endNumber = pb.endNumber
        insulationU = pb.insulationU.takeUnless { it == UNKNOWN_INT }
        phaseAngleClock = pb.phaseAngleClock.takeUnless { it == UNKNOWN_INT }
        r = pb.r.takeUnless { it == UNKNOWN_DOUBLE }
        ratedS = pb.ratedS.takeUnless { it == UNKNOWN_INT }
        ratedU = pb.ratedU.takeUnless { it == UNKNOWN_INT }
        shortTermS = pb.shortTermS.takeUnless { it == UNKNOWN_INT }

        networkService.resolveOrDeferReference(Resolvers.transformerTankInfo(this), pb.transformerTankInfoMRID)
        networkService.resolveOrDeferReference(Resolvers.transformerStarImpedance(this), pb.transformerStarImpedanceMRID)
        networkService.resolveOrDeferReference(Resolvers.energisedEndNoLoadTests(this), pb.energisedEndNoLoadTestsMRID)
        networkService.resolveOrDeferReference(Resolvers.energisedEndShortCircuitTests(this), pb.energisedEndShortCircuitTestsMRID)
        networkService.resolveOrDeferReference(Resolvers.groundedEndShortCircuitTests(this), pb.groundedEndShortCircuitTestsMRID)
        networkService.resolveOrDeferReference(Resolvers.openEndOpenCircuitTests(this), pb.openEndOpenCircuitTestsMRID)
        networkService.resolveOrDeferReference(Resolvers.energisedEndOpenCircuitTests(this), pb.energisedEndOpenCircuitTestsMRID)

        toCim(pb.ai, this, networkService)
    }

fun toCim(pb: PBTransformerTankInfo, networkService: NetworkService): TransformerTankInfo =
    TransformerTankInfo(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.powerTransformerInfo(this), pb.powerTransformerInfoMRID)
        pb.transformerEndInfoMRIDsList.forEach {
            networkService.resolveOrDeferReference(Resolvers.transformerEndInfo(this), it)
        }
        toCim(pb.ai, this, networkService)
    }

fun toCim(pb: PBTransformerTest, cim: TransformerTest, networkService: NetworkService): TransformerTest =
    cim.apply {
        basePower = pb.basePower.takeUnless { it == UNKNOWN_INT }
        temperature = pb.temperature.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBWireInfo, cim: WireInfo, networkService: NetworkService): WireInfo =
    cim.apply {
        ratedCurrent = pb.ratedCurrent.takeUnless { it == UNKNOWN_INT }
        material = WireMaterialKind.valueOf(pb.material.name)
        toCim(pb.ai, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBCableInfo): CableInfo? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBNoLoadTest): NoLoadTest? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBOpenCircuitTest): OpenCircuitTest? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBOverheadWireInfo): OverheadWireInfo? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPowerTransformerInfo): PowerTransformerInfo? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBShortCircuitTest): ShortCircuitTest? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBTransformerEndInfo): TransformerEndInfo? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBTransformerTankInfo): TransformerTankInfo? = tryAddOrNull(toCim(pb, this))

/************ IEC61968 ASSETS ************/

fun toCim(pb: PBAsset, cim: Asset, networkService: NetworkService): Asset =
    cim.apply {
        networkService.resolveOrDeferReference(Resolvers.location(this), pb.locationMRID)
        pb.organisationRoleMRIDsList.forEach {
            networkService.resolveOrDeferReference(Resolvers.organisationRoles(this), it)
        }
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBAssetContainer, cim: AssetContainer, networkService: NetworkService): AssetContainer =
    cim.apply { toCim(pb.at, this, networkService) }

fun toCim(pb: PBAssetInfo, cim: AssetInfo, networkService: NetworkService): AssetInfo =
    cim.apply { toCim(pb.io, this, networkService) }

fun toCim(pb: PBAssetOrganisationRole, cim: AssetOrganisationRole, networkService: NetworkService): AssetOrganisationRole =
    cim.apply { toCim(pb.or, this, networkService) }

fun toCim(pb: PBAssetOwner, networkService: NetworkService): AssetOwner =
    AssetOwner(pb.mRID()).apply {
        toCim(pb.aor, this, networkService)
    }

fun toCim(pb: PBPole, networkService: NetworkService): Pole =
    Pole(pb.mRID()).apply {
        classification = pb.classification.internEmpty()
        pb.streetlightMRIDsList.forEach {
            networkService.resolveOrDeferReference(Resolvers.streetlights(this), it)
        }
        toCim(pb.st, this, networkService)
    }

fun toCim(pb: PBStreetlight, networkService: NetworkService): Streetlight =
    Streetlight(pb.mRID()).apply {
        lampKind = StreetlightLampKind.valueOf(pb.lampKind.name)
        lightRating = pb.lightRating.takeUnless { it == UNKNOWN_UINT }
        networkService.resolveOrDeferReference(Resolvers.pole(this), pb.poleMRID)
        toCim(pb.at, this, networkService)
    }

fun toCim(pb: PBStructure, cim: Structure, networkService: NetworkService): Structure =
    cim.apply { toCim(pb.ac, this, networkService) }

fun NetworkService.addFromPb(pb: PBAssetOwner): AssetOwner? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPole): Pole? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBStreetlight): Streetlight? = tryAddOrNull(toCim(pb, this))

/************ IEC61968 COMMON ************/

fun toCim(pb: PBLocation, networkService: NetworkService): Location =
    Location(pb.mRID()).apply {
        mainAddress = if (pb.hasMainAddress()) toCim(pb.mainAddress) else null
        pb.positionPointsList.forEach { addPoint(toCim(it)) }
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBPositionPoint): PositionPoint =
    PositionPoint(pb.xPosition, pb.yPosition)

fun toCim(pb: PBStreetAddress): StreetAddress =
    StreetAddress(pb.postalCode.internEmpty(), if (pb.hasTownDetail()) toCim(pb.townDetail) else null)

fun toCim(pb: PBTownDetail): TownDetail =
    TownDetail(pb.name.internEmpty(), pb.stateOrProvince.internEmpty())

fun NetworkService.addFromPb(pb: PBOrganisation): Organisation? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBLocation): Location? = tryAddOrNull(toCim(pb, this))

/************ IEC61968 METERING ************/

fun toCim(pb: PBEndDevice, cim: EndDevice, networkService: NetworkService): EndDevice =
    cim.apply {
        pb.usagePointMRIDsList.forEach { usagePointMRID ->
            networkService.resolveOrDeferReference(Resolvers.usagePoints(this), usagePointMRID)
        }
        customerMRID = pb.customerMRID.takeIf { !it.isNullOrBlank() }
        networkService.resolveOrDeferReference(Resolvers.serviceLocation(this), pb.serviceLocationMRID)
        toCim(pb.ac, this, networkService)
    }

fun toCim(pb: PBMeter, networkService: NetworkService): Meter =
    Meter(pb.mRID()).apply {
        toCim(pb.ed, this, networkService)
    }

fun toCim(pb: PBUsagePoint, networkService: NetworkService): UsagePoint =
    UsagePoint(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.usagePointLocation(this), pb.usagePointLocationMRID)
        isVirtual = pb.isVirtual
        connectionCategory = pb.connectionCategory.takeIf { it.isNotBlank() }

        pb.equipmentMRIDsList.forEach { equipmentMRID ->
            networkService.resolveOrDeferReference(Resolvers.equipment(this), equipmentMRID)
        }

        pb.endDeviceMRIDsList.forEach {
            networkService.resolveOrDeferReference(Resolvers.endDevices(this), it)
        }

        toCim(pb.io, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBMeter): Meter? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBUsagePoint): UsagePoint? = tryAddOrNull(toCim(pb, this))

/************ IEC61968 OPERATIONS ************/

fun toCim(pb: PBOperationalRestriction, networkService: NetworkService): OperationalRestriction =
    OperationalRestriction(pb.mRID()).apply {
        toCim(pb.doc, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBOperationalRestriction): OperationalRestriction? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 AUXILIARY EQUIPMENT ************/

fun toCim(pb: PBAuxiliaryEquipment, cim: AuxiliaryEquipment, networkService: NetworkService): AuxiliaryEquipment =
    cim.apply {
        networkService.resolveOrDeferReference(Resolvers.terminal(this), pb.terminalMRID)
        toCim(pb.eq, this, networkService)
    }

fun toCim(pb: PBFaultIndicator, networkService: NetworkService): FaultIndicator =
    FaultIndicator(pb.mRID()).apply {
        toCim(pb.ae, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBFaultIndicator): FaultIndicator? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 CORE ************/

fun toCim(pb: PBAcDcTerminal, cim: AcDcTerminal, networkService: NetworkService): AcDcTerminal =
    cim.apply { toCim(pb.io, this, networkService) }

fun toCim(pb: PBBaseVoltage, networkService: NetworkService): BaseVoltage =
    BaseVoltage(pb.mRID()).apply {
        nominalVoltage = pb.nominalVoltage
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBConductingEquipment, cim: ConductingEquipment, networkService: NetworkService): ConductingEquipment =
    cim.apply {
        networkService.resolveOrDeferReference(Resolvers.baseVoltage(this), pb.baseVoltageMRID)
        pb.terminalMRIDsList.forEach { terminalMRID ->
            networkService.resolveOrDeferReference(Resolvers.terminals(this), terminalMRID)
        }
        toCim(pb.eq, this, networkService)
    }

fun toCim(pb: PBConnectivityNode, networkService: NetworkService): ConnectivityNode =
    ConnectivityNode(pb.mRID()).apply {
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBConnectivityNodeContainer, cim: ConnectivityNodeContainer, networkService: NetworkService): ConnectivityNodeContainer =
    cim.apply { toCim(pb.psr, this, networkService) }

fun toCim(pb: PBEquipment, cim: Equipment, networkService: NetworkService): Equipment =
    cim.apply {
        inService = pb.inService
        normallyInService = pb.normallyInService

        pb.equipmentContainerMRIDsList.forEach { equipmentContainerMRID ->
            networkService.resolveOrDeferReference(Resolvers.containers(this), equipmentContainerMRID)
        }

        pb.usagePointMRIDsList.forEach { usagePointMRID ->
            networkService.resolveOrDeferReference(Resolvers.usagePoints(this), usagePointMRID)
        }

        pb.operationalRestrictionMRIDsList.forEach { operationalRestrictionMRID ->
            networkService.resolveOrDeferReference(Resolvers.operationalRestrictions(this), operationalRestrictionMRID)
        }

        pb.currentFeederMRIDsList.forEach { currentFeederMRID ->
            networkService.resolveOrDeferReference(Resolvers.currentFeeders(this), currentFeederMRID)
        }

        toCim(pb.psr, this, networkService)
    }

fun toCim(pb: PBEquipmentContainer, cim: EquipmentContainer, networkService: NetworkService): EquipmentContainer =
    cim.apply {
        toCim(pb.cnc, this, networkService)
    }

fun toCim(pb: PBFeeder, networkService: NetworkService): Feeder =
    Feeder(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.normalHeadTerminal(this), pb.normalHeadTerminalMRID)
        networkService.resolveOrDeferReference(Resolvers.normalEnergizingSubstation(this), pb.normalEnergizingSubstationMRID)
        toCim(pb.ec, this, networkService)
    }

fun toCim(pb: PBGeographicalRegion, networkService: NetworkService): GeographicalRegion =
    GeographicalRegion(pb.mRID()).apply {
        pb.subGeographicalRegionMRIDsList.forEach { subGeographicalRegionMRID ->
            networkService.resolveOrDeferReference(Resolvers.subGeographicalRegions(this), subGeographicalRegionMRID)
        }
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBPowerSystemResource, cim: PowerSystemResource, networkService: NetworkService): PowerSystemResource =
    cim.apply {
        // NOTE: assetInfoMRID will be handled by classes that use it with specific types.

        networkService.resolveOrDeferReference(Resolvers.location(this), pb.locationMRID)
        numControls = pb.numControls
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBSite, networkService: NetworkService): Site =
    Site(pb.mRID()).apply {
        toCim(pb.ec, this, networkService)
    }

fun toCim(pb: PBSubGeographicalRegion, networkService: NetworkService): SubGeographicalRegion =
    SubGeographicalRegion(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.geographicalRegion(this), pb.geographicalRegionMRID)

        pb.substationMRIDsList.forEach { substationMRID ->
            networkService.resolveOrDeferReference(Resolvers.substations(this), substationMRID)
        }
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBSubstation, networkService: NetworkService): Substation =
    Substation(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.subGeographicalRegion(this), pb.subGeographicalRegionMRID)
        pb.normalEnergizedFeederMRIDsList.forEach { normalEnergizedFeederMRID ->
            networkService.resolveOrDeferReference(Resolvers.normalEnergizingFeeders(this), normalEnergizedFeederMRID)
        }
        pb.loopMRIDsList.forEach { loopMRID ->
            networkService.resolveOrDeferReference(Resolvers.loops(this), loopMRID)
        }
        pb.normalEnergizedLoopMRIDsList.forEach { normalEnergizedLoopMRID ->
            networkService.resolveOrDeferReference(Resolvers.normalEnergizedLoops(this), normalEnergizedLoopMRID)
        }
        pb.circuitMRIDsList.forEach { circuitMRID ->
            networkService.resolveOrDeferReference(Resolvers.circuits(this), circuitMRID)
        }
        toCim(pb.ec, this, networkService)
    }

fun toCim(pb: PBTerminal, networkService: NetworkService): Terminal =
    Terminal(pb.mRID()).apply {
        sequenceNumber = pb.sequenceNumber
        networkService.resolveOrDeferReference(Resolvers.conductingEquipment(this), pb.conductingEquipmentMRID)

        phases = PhaseCode.valueOf(pb.phases.name)
        tracedPhases.normalStatusInternal = pb.tracedPhases.normalStatus
        tracedPhases.currentStatusInternal = pb.tracedPhases.currentStatus
        networkService.resolveOrDeferReference(Resolvers.connectivityNode(this), pb.connectivityNodeMRID)
        toCim(pb.ad, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBBaseVoltage): BaseVoltage? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBConnectivityNode): ConnectivityNode? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBFeeder): Feeder? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBGeographicalRegion): GeographicalRegion? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBNameType): NameType = toCim(pb, this) // Special case
fun NetworkService.addFromPb(pb: PBSite): Site? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBSubGeographicalRegion): SubGeographicalRegion? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBSubstation): Substation? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBTerminal): Terminal? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 BASE EQUIVALENTS ************/

fun toCim(pb: PBEquivalentBranch, networkService: NetworkService): EquivalentBranch =
    EquivalentBranch(pb.mRID()).apply {
        negativeR12 = pb.negativeR12.takeUnless { it == UNKNOWN_DOUBLE }
        negativeR21 = pb.negativeR21.takeUnless { it == UNKNOWN_DOUBLE }
        negativeX12 = pb.negativeX12.takeUnless { it == UNKNOWN_DOUBLE }
        negativeX21 = pb.negativeX21.takeUnless { it == UNKNOWN_DOUBLE }
        positiveR12 = pb.positiveR12.takeUnless { it == UNKNOWN_DOUBLE }
        positiveR21 = pb.positiveR21.takeUnless { it == UNKNOWN_DOUBLE }
        positiveX12 = pb.positiveX12.takeUnless { it == UNKNOWN_DOUBLE }
        positiveX21 = pb.positiveX21.takeUnless { it == UNKNOWN_DOUBLE }
        r = pb.r.takeUnless { it == UNKNOWN_DOUBLE }
        r21 = pb.r21.takeUnless { it == UNKNOWN_DOUBLE }
        x = pb.x.takeUnless { it == UNKNOWN_DOUBLE }
        x21 = pb.x21.takeUnless { it == UNKNOWN_DOUBLE }
        zeroR12 = pb.zeroR12.takeUnless { it == UNKNOWN_DOUBLE }
        zeroR21 = pb.zeroR21.takeUnless { it == UNKNOWN_DOUBLE }
        zeroX12 = pb.zeroX12.takeUnless { it == UNKNOWN_DOUBLE }
        zeroX21 = pb.zeroX21.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.ee, this, networkService)
    }

fun toCim(pb: PBEquivalentEquipment, cim: EquivalentEquipment, networkService: NetworkService): EquivalentEquipment =
    cim.apply { toCim(pb.ce, this, networkService) }

fun NetworkService.addFromPb(pb: PBEquivalentBranch): EquivalentBranch? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 MEAS ************/

fun toCim(pb: PBControl, networkService: NetworkService): Control =
    Control(pb.mRID()).apply {
        powerSystemResourceMRID = pb.powerSystemResourceMRID.takeIf { it.isNotBlank() }
        networkService.resolveOrDeferReference(Resolvers.remoteControl(this), pb.remoteControlMRID)
        toCim(pb.ip, this, networkService)
    }

fun toCim(pb: PBIoPoint, cim: IoPoint, networkService: NetworkService): IoPoint =
    cim.apply { toCim(pb.io, this, networkService) }

fun toCim(pb: PBMeasurement, cim: Measurement, networkService: NetworkService) {
    cim.apply {
        powerSystemResourceMRID = pb.powerSystemResourceMRID.takeIf { it.isNotBlank() }
        networkService.resolveOrDeferReference(Resolvers.remoteSource(this), pb.remoteSourceMRID)
        terminalMRID = pb.terminalMRID.takeIf { it.isNotBlank() }
        phases = PhaseCode.valueOf(pb.phases.name)
        unitSymbol = UnitSymbol.valueOf(pb.unitSymbol.name)
        toCim(pb.io, this, networkService)
    }
}

fun toCim(pb: PBAccumulator, networkService: NetworkService): Accumulator =
    Accumulator(pb.measurement.mRID()).apply {
        toCim(pb.measurement, this, networkService)
    }

fun toCim(pb: PBAnalog, networkService: NetworkService): Analog =
    Analog(pb.measurement.mRID()).apply {
        toCim(pb.measurement, this, networkService)
        positiveFlowIn = pb.positiveFlowIn
    }

fun toCim(pb: PBDiscrete, networkService: NetworkService): Discrete =
    Discrete(pb.measurement.mRID()).apply {
        toCim(pb.measurement, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBControl): Control? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBAnalog): Analog? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBAccumulator): Accumulator? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBDiscrete): Discrete? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 SCADA ************/

fun toCim(pb: PBRemoteControl, networkService: NetworkService): RemoteControl =
    RemoteControl(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.control(this), pb.controlMRID)
        toCim(pb.rp, this, networkService)
    }

fun toCim(pb: PBRemotePoint, cim: RemotePoint, networkService: NetworkService): RemotePoint =
    cim.apply { toCim(pb.io, this, networkService) }

fun toCim(pb: PBRemoteSource, networkService: NetworkService): RemoteSource =
    RemoteSource(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.measurement(this), pb.measurementMRID)
        toCim(pb.rp, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBRemoteControl): RemoteControl? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBRemoteSource): RemoteSource? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 WIRES GENERATION PRODUCTION ************/

fun toCim(pb: PBPowerElectronicsUnit, cim: PowerElectronicsUnit, networkService: NetworkService): PowerElectronicsUnit =
    cim.apply {
        networkService.resolveOrDeferReference(Resolvers.powerElectronicsConnection(this), pb.powerElectronicsConnectionMRID)
        maxP = pb.maxP.takeUnless { it == UNKNOWN_INT }
        minP = pb.minP.takeUnless { it == UNKNOWN_INT }
        toCim(pb.eq, this, networkService)
    }

fun toCim(pb: PBBatteryUnit, networkService: NetworkService): BatteryUnit =
    BatteryUnit(pb.mRID()).apply {
        batteryState = BatteryStateKind.valueOf(pb.batteryState.name)
        ratedE = pb.ratedE.takeUnless { it == UNKNOWN_LONG }
        storedE = pb.storedE.takeUnless { it == UNKNOWN_LONG }
        toCim(pb.peu, this, networkService)
    }

fun toCim(pb: PBPhotoVoltaicUnit, networkService: NetworkService): PhotoVoltaicUnit =
    PhotoVoltaicUnit(pb.mRID()).apply {
        toCim(pb.peu, this, networkService)
    }

fun toCim(pb: PBPowerElectronicsWindUnit, networkService: NetworkService): PowerElectronicsWindUnit =
    PowerElectronicsWindUnit(pb.mRID()).apply {
        toCim(pb.peu, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBBatteryUnit): BatteryUnit? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPhotoVoltaicUnit): PhotoVoltaicUnit? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPowerElectronicsWindUnit): PowerElectronicsWindUnit? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 WIRES ************/

fun toCim(pb: PBAcLineSegment, networkService: NetworkService): AcLineSegment =
    AcLineSegment(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.perLengthSequenceImpedance(this), pb.perLengthSequenceImpedanceMRID)
        toCim(pb.cd, this, networkService)
    }

fun toCim(pb: PBBreaker, networkService: NetworkService): Breaker =
    Breaker(pb.mRID()).apply {
        toCim(pb.sw, this, networkService)
    }

fun toCim(pb: PBLoadBreakSwitch, networkService: NetworkService): LoadBreakSwitch =
    LoadBreakSwitch(pb.mRID()).apply {
        toCim(pb.ps, this, networkService)
    }

fun toCim(pb: PBBusbarSection, networkService: NetworkService): BusbarSection =
    BusbarSection(pb.mRID()).apply {
        toCim(pb.cn, this, networkService)
    }

fun toCim(pb: PBConductor, cim: Conductor, networkService: NetworkService): Conductor =
    cim.apply {
        length = pb.length.takeUnless { it == UNKNOWN_DOUBLE }
        networkService.resolveOrDeferReference(Resolvers.assetInfo(this), pb.assetInfoMRID())
        toCim(pb.ce, this, networkService)
    }

fun toCim(pb: PBConnector, cim: Connector, networkService: NetworkService): Connector =
    cim.apply { toCim(pb.ce, this, networkService) }

fun toCim(pb: PBDisconnector, networkService: NetworkService): Disconnector =
    Disconnector(pb.mRID()).apply {
        toCim(pb.sw, this, networkService)
    }

fun toCim(pb: PBEnergyConnection, cim: EnergyConnection, networkService: NetworkService): EnergyConnection =
    cim.apply { toCim(pb.ce, this, networkService) }

fun toCim(pb: PBEnergyConsumer, networkService: NetworkService): EnergyConsumer =
    EnergyConsumer(pb.mRID()).apply {

        pb.energyConsumerPhasesMRIDsList.forEach { energyConsumerPhasesMRID ->
            networkService.resolveOrDeferReference(Resolvers.phases(this), energyConsumerPhasesMRID)
        }
        customerCount = pb.customerCount.takeUnless { it == UNKNOWN_INT }
        grounded = pb.grounded
        p = pb.p.takeUnless { it == UNKNOWN_DOUBLE }
        pFixed = pb.pFixed.takeUnless { it == UNKNOWN_DOUBLE }
        phaseConnection = PhaseShuntConnectionKind.valueOf(pb.phaseConnection.name)
        q = pb.q.takeUnless { it == UNKNOWN_DOUBLE }
        qFixed = pb.qFixed.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.ec, this, networkService)
    }

fun toCim(pb: PBEnergyConsumerPhase, networkService: NetworkService): EnergyConsumerPhase =
    EnergyConsumerPhase(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.energyConsumer(this), pb.energyConsumerMRID)
        phase = SinglePhaseKind.valueOf(pb.phase.name)
        p = pb.p.takeUnless { it == UNKNOWN_DOUBLE }
        pFixed = pb.pFixed.takeUnless { it == UNKNOWN_DOUBLE }
        q = pb.q.takeUnless { it == UNKNOWN_DOUBLE }
        qFixed = pb.qFixed.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.psr, this, networkService)
    }

fun toCim(pb: PBEnergySource, networkService: NetworkService): EnergySource =
    EnergySource(pb.mRID()).apply {
        pb.energySourcePhasesMRIDsList.forEach { energySourcePhasesMRID ->
            networkService.resolveOrDeferReference(Resolvers.phases(this), energySourcePhasesMRID)
        }
        activePower = pb.activePower.takeUnless { it == UNKNOWN_DOUBLE }
        reactivePower = pb.reactivePower.takeUnless { it == UNKNOWN_DOUBLE }
        voltageAngle = pb.voltageAngle.takeUnless { it == UNKNOWN_DOUBLE }
        voltageMagnitude = pb.voltageMagnitude.takeUnless { it == UNKNOWN_DOUBLE }
        r = pb.r.takeUnless { it == UNKNOWN_DOUBLE }
        x = pb.x.takeUnless { it == UNKNOWN_DOUBLE }
        pMax = pb.pMax.takeUnless { it == UNKNOWN_DOUBLE }
        pMin = pb.pMin.takeUnless { it == UNKNOWN_DOUBLE }
        r0 = pb.r0.takeUnless { it == UNKNOWN_DOUBLE }
        rn = pb.rn.takeUnless { it == UNKNOWN_DOUBLE }
        x0 = pb.x0.takeUnless { it == UNKNOWN_DOUBLE }
        xn = pb.xn.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.ec, this, networkService)
    }

fun toCim(pb: PBEnergySourcePhase, networkService: NetworkService): EnergySourcePhase =
    EnergySourcePhase(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.energySource(this), pb.energySourceMRID)
        phase = SinglePhaseKind.valueOf(pb.phase.name)
        toCim(pb.psr, this, networkService)
    }

fun toCim(pb: PBFuse, networkService: NetworkService): Fuse =
    Fuse(pb.mRID()).apply {
        toCim(pb.sw, this, networkService)
    }

fun toCim(pb: PBJumper, networkService: NetworkService): Jumper =
    Jumper(pb.mRID()).apply {
        toCim(pb.sw, this, networkService)
    }

fun toCim(pb: PBJunction, networkService: NetworkService): Junction =
    Junction(pb.mRID()).apply {
        toCim(pb.cn, this, networkService)
    }

fun toCim(pb: PBLine, cim: Line, networkService: NetworkService): Line =
    cim.apply { toCim(pb.ec, this, networkService) }

fun toCim(pb: PBLinearShuntCompensator, networkService: NetworkService): LinearShuntCompensator =
    LinearShuntCompensator(pb.mRID()).apply {
        b0PerSection = pb.b0PerSection.takeUnless { it == UNKNOWN_DOUBLE }
        bPerSection = pb.bPerSection.takeUnless { it == UNKNOWN_DOUBLE }
        g0PerSection = pb.g0PerSection.takeUnless { it == UNKNOWN_DOUBLE }
        gPerSection = pb.gPerSection.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.sc, this, networkService)
    }

fun toCim(pb: PBPerLengthLineParameter, cim: PerLengthLineParameter, networkService: NetworkService): PerLengthLineParameter =
    cim.apply { toCim(pb.io, this, networkService) }

fun toCim(pb: PBPerLengthImpedance, cim: PerLengthImpedance, networkService: NetworkService): PerLengthImpedance =
    cim.apply { toCim(pb.lp, cim, networkService) }

fun toCim(pb: PBPerLengthSequenceImpedance, networkService: NetworkService): PerLengthSequenceImpedance =
    PerLengthSequenceImpedance(pb.mRID()).apply {
        r = pb.r.takeUnless { it == UNKNOWN_DOUBLE }
        x = pb.x.takeUnless { it == UNKNOWN_DOUBLE }
        r0 = pb.r0.takeUnless { it == UNKNOWN_DOUBLE }
        x0 = pb.x0.takeUnless { it == UNKNOWN_DOUBLE }
        bch = pb.bch.takeUnless { it == UNKNOWN_DOUBLE }
        gch = pb.gch.takeUnless { it == UNKNOWN_DOUBLE }
        b0ch = pb.b0Ch.takeUnless { it == UNKNOWN_DOUBLE }
        g0ch = pb.g0Ch.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.pli, this, networkService)
    }

fun toCim(pb: PBPowerElectronicsConnection, networkService: NetworkService): PowerElectronicsConnection =
    PowerElectronicsConnection(pb.mRID()).apply {
        pb.powerElectronicsUnitMRIDsList.forEach { powerElectronicsUnitMRID ->
            networkService.resolveOrDeferReference(Resolvers.powerElectronicsUnit(this), powerElectronicsUnitMRID)
        }
        pb.powerElectronicsConnectionPhaseMRIDsList.forEach { powerElectronicsConnectionPhaseMRID ->
            networkService.resolveOrDeferReference(Resolvers.powerElectronicsConnectionPhase(this), powerElectronicsConnectionPhaseMRID)
        }
        maxIFault = pb.maxIFault.takeUnless { it == UNKNOWN_INT }
        maxQ = pb.maxQ.takeUnless { it == UNKNOWN_DOUBLE }
        minQ = pb.minQ.takeUnless { it == UNKNOWN_DOUBLE }
        p = pb.p.takeUnless { it == UNKNOWN_DOUBLE }
        q = pb.q.takeUnless { it == UNKNOWN_DOUBLE }
        ratedS = pb.ratedS.takeUnless { it == UNKNOWN_INT }
        ratedU = pb.ratedU.takeUnless { it == UNKNOWN_INT }
        toCim(pb.rce, this, networkService)
    }

fun toCim(pb: PBPowerElectronicsConnectionPhase, networkService: NetworkService): PowerElectronicsConnectionPhase =
    PowerElectronicsConnectionPhase(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.powerElectronicsConnection(this), pb.powerElectronicsConnectionMRID)
        p = pb.p.takeUnless { it == UNKNOWN_DOUBLE }
        phase = SinglePhaseKind.valueOf(pb.phase.name)
        q = pb.q.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.psr, this, networkService)
    }

fun toCim(pb: PBPowerTransformer, networkService: NetworkService): PowerTransformer =
    PowerTransformer(pb.mRID()).apply {
        pb.powerTransformerEndMRIDsList.forEach { endMRID ->
            networkService.resolveOrDeferReference(Resolvers.ends(this), endMRID)
        }
        vectorGroup = VectorGroup.valueOf(pb.vectorGroup.name)
        transformerUtilisation = pb.transformerUtilisation.takeUnless { it == UNKNOWN_DOUBLE }
        networkService.resolveOrDeferReference(Resolvers.assetInfo(this), pb.assetInfoMRID())
        toCim(pb.ce, this, networkService)
    }

fun toCim(pb: PBPowerTransformerEnd, networkService: NetworkService): PowerTransformerEnd =
    PowerTransformerEnd(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.powerTransformer(this), pb.powerTransformerMRID)
        ratedS = pb.ratedS.takeUnless { it == UNKNOWN_INT }
        ratedU = pb.ratedU.takeUnless { it == UNKNOWN_INT }
        r = pb.r.takeUnless { it == UNKNOWN_DOUBLE }
        r0 = pb.r0.takeUnless { it == UNKNOWN_DOUBLE }
        x = pb.x.takeUnless { it == UNKNOWN_DOUBLE }
        x0 = pb.x0.takeUnless { it == UNKNOWN_DOUBLE }
        connectionKind = WindingConnection.valueOf(pb.connectionKind.name)
        b = pb.b.takeUnless { it == UNKNOWN_DOUBLE }
        b0 = pb.b0.takeUnless { it == UNKNOWN_DOUBLE }
        g = pb.g.takeUnless { it == UNKNOWN_DOUBLE }
        g0 = pb.g0.takeUnless { it == UNKNOWN_DOUBLE }
        phaseAngleClock = pb.phaseAngleClock.takeUnless { it == UNKNOWN_INT }
        toCim(pb.te, this, networkService)
    }

fun toCim(pb: PBProtectedSwitch, cim: ProtectedSwitch, networkService: NetworkService): ProtectedSwitch =
    cim.apply { toCim(pb.sw, this, networkService) }

fun toCim(pb: PBRatioTapChanger, networkService: NetworkService): RatioTapChanger =
    RatioTapChanger(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.transformerEnd(this), pb.transformerEndMRID)
        stepVoltageIncrement = pb.stepVoltageIncrement.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.tc, this, networkService)
    }

fun toCim(pb: PBRecloser, networkService: NetworkService): Recloser =
    Recloser(pb.mRID()).apply {
        toCim(pb.sw, this, networkService)
    }

fun toCim(pb: PBRegulatingCondEq, cim: RegulatingCondEq, networkService: NetworkService): RegulatingCondEq =
    cim.apply {
        controlEnabled = pb.controlEnabled
        toCim(pb.ec, this, networkService)
    }

fun toCim(pb: PBShuntCompensator, cim: ShuntCompensator, networkService: NetworkService): ShuntCompensator =
    cim.apply {
        sections = pb.sections.takeUnless { it == UNKNOWN_DOUBLE }
        grounded = pb.grounded
        nomU = pb.nomU.takeUnless { it == UNKNOWN_INT }
        phaseConnection = PhaseShuntConnectionKind.valueOf(pb.phaseConnection.name)
        toCim(pb.rce, this, networkService)
    }

fun toCim(pb: PBSwitch, cim: Switch, networkService: NetworkService): Switch =
    cim.apply {
        setNormallyOpen(pb.normalOpen)
        setOpen(pb.open)
        // when unganged support is added to protobuf
        // normalOpen = pb.normalOpen
        // open = pb.open
        toCim(pb.ce, this, networkService)
    }

fun toCim(pb: PBTapChanger, cim: TapChanger, networkService: NetworkService): TapChanger =
    cim.apply {
        highStep = pb.highStep.takeUnless { it == UNKNOWN_INT }
        lowStep = pb.lowStep.takeUnless { it == UNKNOWN_INT }
        step = pb.step.takeUnless { it == UNKNOWN_DOUBLE }
        neutralStep = pb.neutralStep.takeUnless { it == UNKNOWN_INT }
        neutralU = pb.neutralU.takeUnless { it == UNKNOWN_INT }
        normalStep = pb.normalStep.takeUnless { it == UNKNOWN_INT }
        controlEnabled = pb.controlEnabled
        toCim(pb.psr, this, networkService)
    }

fun toCim(pb: PBTransformerEnd, cim: TransformerEnd, networkService: NetworkService): TransformerEnd =
    cim.apply {
        networkService.resolveOrDeferReference(Resolvers.terminal(this), pb.terminalMRID)
        networkService.resolveOrDeferReference(Resolvers.baseVoltage(this), pb.baseVoltageMRID)
        networkService.resolveOrDeferReference(Resolvers.ratioTapChanger(this), pb.ratioTapChangerMRID)
        networkService.resolveOrDeferReference(Resolvers.starImpedance(this), pb.starImpedanceMRID)
        endNumber = pb.endNumber
        grounded = pb.grounded
        rGround = pb.rGround.takeUnless { it == UNKNOWN_DOUBLE }
        xGround = pb.xGround.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.io, this, networkService)
    }

fun toCim(pb: PBTransformerStarImpedance, networkService: NetworkService): TransformerStarImpedance =
    TransformerStarImpedance(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.transformerEndInfo(this), pb.transformerEndInfoMRID)
        r = pb.r.takeUnless { it == UNKNOWN_DOUBLE }
        r0 = pb.r0.takeUnless { it == UNKNOWN_DOUBLE }
        x = pb.x.takeUnless { it == UNKNOWN_DOUBLE }
        x0 = pb.x0.takeUnless { it == UNKNOWN_DOUBLE }
        toCim(pb.io, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBAcLineSegment): AcLineSegment? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBBreaker): Breaker? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBBusbarSection): BusbarSection? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBDisconnector): Disconnector? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBEnergyConsumer): EnergyConsumer? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBEnergyConsumerPhase): EnergyConsumerPhase? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBEnergySource): EnergySource? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBEnergySourcePhase): EnergySourcePhase? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBFuse): Fuse? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBJumper): Jumper? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBJunction): Junction? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBLinearShuntCompensator): LinearShuntCompensator? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBLoadBreakSwitch): LoadBreakSwitch? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPerLengthSequenceImpedance): PerLengthSequenceImpedance? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPowerElectronicsConnection): PowerElectronicsConnection? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPowerElectronicsConnectionPhase): PowerElectronicsConnectionPhase? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPowerTransformer): PowerTransformer? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBPowerTransformerEnd): PowerTransformerEnd? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBRatioTapChanger): RatioTapChanger? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBRecloser): Recloser? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBTransformerStarImpedance): TransformerStarImpedance? = tryAddOrNull(toCim(pb, this))

/************ IEC61970 InfIEC61970 Feeder ************/

fun toCim(pb: PBCircuit, networkService: NetworkService): Circuit =
    Circuit(pb.mRID()).apply {
        networkService.resolveOrDeferReference(Resolvers.loop(this), pb.loopMRID)
        pb.endTerminalMRIDsList.forEach { endTerminalMRID ->
            networkService.resolveOrDeferReference(Resolvers.endTerminal(this), endTerminalMRID)
        }
        pb.endSubstationMRIDsList.forEach { endSubstationMRID ->
            networkService.resolveOrDeferReference(Resolvers.endSubstation(this), endSubstationMRID)
        }

        toCim(pb.l, this, networkService)
    }

fun toCim(pb: PBLoop, networkService: NetworkService): Loop =
    Loop(pb.mRID()).apply {
        pb.circuitMRIDsList.forEach { circuitMRID ->
            networkService.resolveOrDeferReference(Resolvers.circuits(this), circuitMRID)
        }
        pb.substationMRIDsList.forEach { substationMRID ->
            networkService.resolveOrDeferReference(Resolvers.substations(this), substationMRID)
        }
        pb.normalEnergizingSubstationMRIDsList.forEach { normalEnergizingSubstationMRID ->
            networkService.resolveOrDeferReference(Resolvers.normalEnergizingSubstations(this), normalEnergizingSubstationMRID)
        }

        toCim(pb.io, this, networkService)
    }

fun NetworkService.addFromPb(pb: PBCircuit): Circuit? = tryAddOrNull(toCim(pb, this))
fun NetworkService.addFromPb(pb: PBLoop): Loop? = tryAddOrNull(toCim(pb, this))

/************ Class for Java friendly usage ************/

class NetworkProtoToCim(val networkService: NetworkService) : BaseProtoToCim() {

    // IEC61968 ASSET INFO
    fun addFromPb(pb: PBCableInfo): CableInfo? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBNoLoadTest): NoLoadTest? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBOpenCircuitTest): OpenCircuitTest? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBOverheadWireInfo): OverheadWireInfo? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPowerTransformerInfo): PowerTransformerInfo? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBShortCircuitTest): ShortCircuitTest? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBTransformerEndInfo): TransformerEndInfo? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBTransformerTankInfo): TransformerTankInfo? = networkService.addFromPb(pb)

    // IEC61968 ASSETS
    fun addFromPb(pb: PBAssetOwner): AssetOwner? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPole): Pole? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBStreetlight): Streetlight? = networkService.addFromPb(pb)

    // IEC61968 COMMON
    fun addFromPb(pb: PBOrganisation): Organisation? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBLocation): Location? = networkService.addFromPb(pb)

    // IEC61968 METERING
    fun addFromPb(pb: PBMeter): Meter? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBUsagePoint): UsagePoint? = networkService.addFromPb(pb)

    // IEC61968 OPERATIONS
    fun addFromPb(pb: PBOperationalRestriction): OperationalRestriction? = networkService.addFromPb(pb)

    // IEC61970 BASE AUXILIARY EQUIPMENT
    fun addFromPb(pb: PBFaultIndicator): FaultIndicator? = networkService.addFromPb(pb)

    // IEC61970 BASE CORE
    fun addFromPb(pb: PBBaseVoltage): BaseVoltage? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBConnectivityNode): ConnectivityNode? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBFeeder): Feeder? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBGeographicalRegion): GeographicalRegion? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBNameType): NameType = networkService.addFromPb(pb)
    fun addFromPb(pb: PBSite): Site? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBSubGeographicalRegion): SubGeographicalRegion? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBSubstation): Substation? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBTerminal): Terminal? = networkService.addFromPb(pb)

    // IEC61970 BASE BASE EQUIVALENTS
    fun addFromPb(pb: PBEquivalentBranch): EquivalentBranch? = networkService.addFromPb(pb)

    // IEC61970 BASE MEAS
    fun addFromPb(pb: PBControl): Control? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBAnalog): Analog? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBAccumulator): Accumulator? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBDiscrete): Discrete? = networkService.addFromPb(pb)

    // IEC61970 BASE SCADA
    fun addFromPb(pb: PBRemoteControl): RemoteControl? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBRemoteSource): RemoteSource? = networkService.addFromPb(pb)

    // IEC61970 BASE WIRES GENERATION PRODUCTION
    fun addFromPb(pb: PBBatteryUnit): BatteryUnit? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPhotoVoltaicUnit): PhotoVoltaicUnit? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPowerElectronicsWindUnit): PowerElectronicsWindUnit? = networkService.addFromPb(pb)

    // IEC61970 BASE WIRES
    fun addFromPb(pb: PBAcLineSegment): AcLineSegment? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBBreaker): Breaker? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBBusbarSection): BusbarSection? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBDisconnector): Disconnector? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBEnergyConsumer): EnergyConsumer? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBEnergyConsumerPhase): EnergyConsumerPhase? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBEnergySource): EnergySource? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBEnergySourcePhase): EnergySourcePhase? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBFuse): Fuse? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBJumper): Jumper? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBJunction): Junction? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBLinearShuntCompensator): LinearShuntCompensator? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBLoadBreakSwitch): LoadBreakSwitch? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPerLengthSequenceImpedance): PerLengthSequenceImpedance? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPowerElectronicsConnection): PowerElectronicsConnection? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPowerElectronicsConnectionPhase): PowerElectronicsConnectionPhase? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPowerTransformer): PowerTransformer? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBPowerTransformerEnd): PowerTransformerEnd? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBRatioTapChanger): RatioTapChanger? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBRecloser): Recloser? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBTransformerStarImpedance): TransformerStarImpedance? = networkService.addFromPb(pb)

    // IEC61970 InfIEC61970 Feeder
    fun addFromPb(pb: PBCircuit): Circuit? = networkService.addFromPb(pb)
    fun addFromPb(pb: PBLoop): Loop? = networkService.addFromPb(pb)

}
