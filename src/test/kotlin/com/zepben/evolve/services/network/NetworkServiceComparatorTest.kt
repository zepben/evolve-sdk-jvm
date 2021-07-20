/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.network

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
import com.zepben.evolve.services.common.BaseServiceComparatorTest
import com.zepben.evolve.services.common.ObjectDifference
import com.zepben.evolve.services.common.ValueDifference
import com.zepben.evolve.services.network.tracing.phases.PhaseDirection
import com.zepben.evolve.services.network.tracing.phases.TracedPhases
import com.zepben.evolve.utils.ServiceComparatorValidator
import org.junit.jupiter.api.Test

@Suppress("SameParameterValue")
internal class NetworkServiceComparatorTest : BaseServiceComparatorTest() {

    override val comparatorValidator: ServiceComparatorValidator<NetworkService, NetworkServiceComparator> = ServiceComparatorValidator(
        { NetworkService() },
        { options -> NetworkServiceComparator(options) }
    )

    /************ IEC61968 ASSET INFO ************/

    @Test
    internal fun compareCableInfo() {
        compareWireInfo { CableInfo(mRID = it) }
    }

    @Test
    internal fun compareNoLoadTest() {
        compareTransformerTest { NoLoadTest(it) }

        comparatorValidator.validateProperty(NoLoadTest::energisedEndVoltage, { NoLoadTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(NoLoadTest::excitingCurrent, { NoLoadTest(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(NoLoadTest::excitingCurrentZero, { NoLoadTest(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(NoLoadTest::loss, { NoLoadTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(NoLoadTest::lossZero, { NoLoadTest(it) }, { 1 }, { 2 })
    }

    @Test
    internal fun compareOpenCircuitTest() {
        compareTransformerTest { OpenCircuitTest(it) }

        comparatorValidator.validateProperty(OpenCircuitTest::energisedEndStep, { OpenCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(OpenCircuitTest::energisedEndVoltage, { OpenCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(OpenCircuitTest::openEndStep, { OpenCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(OpenCircuitTest::openEndVoltage, { OpenCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(OpenCircuitTest::phaseShift, { OpenCircuitTest(it) }, { 1.0 }, { 2.0 })
    }

    @Test
    internal fun compareOverheadWireInfo() {
        compareWireInfo { OverheadWireInfo(mRID = it) }
    }

    @Test
    internal fun comparePowerTransformerInfo() {
        compareAssetInfo { PowerTransformerInfo(mRID = it) }

        comparatorValidator.validateCollection(
            PowerTransformerInfo::transformerTankInfos,
            PowerTransformerInfo::addTransformerTankInfo,
            { PowerTransformerInfo(it) },
            { TransformerTankInfo("tti1") },
            { TransformerTankInfo("tti2") })
    }

    @Test
    internal fun compareShortCircuitTest() {
        compareTransformerTest { ShortCircuitTest(it) }

        comparatorValidator.validateProperty(ShortCircuitTest::current, { ShortCircuitTest(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(ShortCircuitTest::energisedEndStep, { ShortCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(ShortCircuitTest::groundedEndStep, { ShortCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(ShortCircuitTest::leakageImpedance, { ShortCircuitTest(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(ShortCircuitTest::leakageImpedanceZero, { ShortCircuitTest(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(ShortCircuitTest::loss, { ShortCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(ShortCircuitTest::lossZero, { ShortCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(ShortCircuitTest::power, { ShortCircuitTest(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(ShortCircuitTest::voltage, { ShortCircuitTest(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(ShortCircuitTest::voltageOhmicPart, { ShortCircuitTest(it) }, { 1.0 }, { 2.0 })
    }

    @Test
    internal fun compareTransformerEndInfo() {
        compareAssetInfo { TransformerEndInfo(it) }

        comparatorValidator.validateProperty(TransformerEndInfo::connectionKind, { TransformerEndInfo(it) }, { WindingConnection.D }, { WindingConnection.Y })
        comparatorValidator.validateProperty(TransformerEndInfo::emergencyS, { TransformerEndInfo(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TransformerEndInfo::endNumber, { TransformerEndInfo(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TransformerEndInfo::insulationU, { TransformerEndInfo(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TransformerEndInfo::phaseAngleClock, { TransformerEndInfo(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TransformerEndInfo::r, { TransformerEndInfo(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(TransformerEndInfo::ratedS, { TransformerEndInfo(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TransformerEndInfo::ratedU, { TransformerEndInfo(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TransformerEndInfo::shortTermS, { TransformerEndInfo(it) }, { 1 }, { 2 })

        comparatorValidator.validateProperty(
            TransformerEndInfo::transformerStarImpedance,
            { TransformerEndInfo(it) },
            { TransformerStarImpedance("tsi1") },
            { TransformerStarImpedance("tsi2") }
        )
    }

    @Test
    internal fun compareTransformerTankInfo() {
        compareAssetInfo { TransformerTankInfo(it) }

        comparatorValidator.validateCollection(
            TransformerTankInfo::transformerEndInfos,
            TransformerTankInfo::addTransformerEndInfo,
            { TransformerTankInfo(it) },
            { TransformerEndInfo("tei1") },
            { TransformerEndInfo("tei2") })
    }

    private fun compareTransformerTest(createTransformerTest: (String) -> TransformerTest) {
        compareIdentifiedObject(createTransformerTest)

        comparatorValidator.validateProperty(TransformerTest::basePower, createTransformerTest, { 1 }, { 2 })
        comparatorValidator.validateProperty(TransformerTest::temperature, createTransformerTest, { 1.0 }, { 2.0 })
    }

    private fun compareWireInfo(createWireInfo: (String) -> WireInfo) {
        compareAssetInfo(createWireInfo)

        comparatorValidator.validateProperty(WireInfo::ratedCurrent, createWireInfo, { 1 }, { 2 })
        comparatorValidator.validateProperty(WireInfo::material, createWireInfo, { WireMaterialKind.aluminum }, { WireMaterialKind.copperCadmium })
    }

    /************ IEC61968 ASSETS ************/

    private fun compareAsset(createAsset: (String) -> Asset) {
        compareIdentifiedObject(createAsset)

        comparatorValidator.validateCollection(Asset::organisationRoles, Asset::addOrganisationRole, createAsset, { AssetOwner("a1") }, { AssetOwner("a2") })
        comparatorValidator.validateProperty(Asset::location, createAsset, { Location("l1") }, { Location("l2") })
    }

    private fun compareAssetContainer(createAssetContainer: (String) -> AssetContainer) {
        compareAsset(createAssetContainer)
    }

    private fun compareAssetInfo(createAssetInfo: (String) -> AssetInfo) {
        compareIdentifiedObject(createAssetInfo)
    }

    private fun compareAssetOrganisationRole(createAssetOrganisationRole: (String) -> AssetOrganisationRole) {
        compareOrganisationRole(createAssetOrganisationRole)
    }

    @Test
    internal fun compareAssetOwner() {
        compareAssetOrganisationRole { AssetOwner(mRID = it) }
    }

    @Test
    internal fun comparePole() {
        compareStructure { Pole(mRID = it) }
        comparatorValidator.validateProperty(Pole::classification, { Pole(it) }, { "c1" }, { "c2" })
        comparatorValidator.validateCollection(Pole::streetlights, Pole::addStreetlight, { Pole(it) }, { Streetlight("sl1") }, { Streetlight("sl2") })
    }

    @Test
    internal fun compareStreetlight() {
        compareAsset { Streetlight(mRID = it) }

        comparatorValidator.validateProperty(
            Streetlight::lampKind,
            { Streetlight(it) },
            { StreetlightLampKind.HIGH_PRESSURE_SODIUM },
            { StreetlightLampKind.MERCURY_VAPOR })
        comparatorValidator.validateProperty(Streetlight::lightRating, { Streetlight(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(Streetlight::pole, { Streetlight(it) }, { Pole("x") }, { Pole("y") })
    }

    private fun compareStructure(createStructure: (String) -> Structure) {
        compareAssetContainer(createStructure)
    }

    /************ IEC61968 COMMON ************/

    @Test
    internal fun compareLocation() {
        compareIdentifiedObject { Location(it) }

        comparatorValidator.validateProperty(
            Location::mainAddress,
            { Location(it) },
            { StreetAddress("1234", TownDetail("town", "state")) },
            { StreetAddress("1234", TownDetail("other", "state")) })
        comparatorValidator.validateIndexedCollection(
            Location::points,
            Location::addPoint,
            { Location(it) },
            { PositionPoint(1.0, 2.0) },
            { PositionPoint(3.0, 4.0) })
    }

    /************ IEC61968 METERING ************/

    private fun compareEndDevice(createEndDevice: (String) -> EndDevice) {
        compareAssetContainer(createEndDevice)

        comparatorValidator.validateProperty(EndDevice::customerMRID, createEndDevice, { "customer1" }, { "customer2" })
        comparatorValidator.validateProperty(EndDevice::serviceLocation, createEndDevice, { Location("l1") }, { Location("l2") })
        comparatorValidator.validateCollection(
            EndDevice::usagePoints,
            EndDevice::addUsagePoint,
            createEndDevice,
            { UsagePoint("up1") },
            { UsagePoint("up2") },
            NetworkServiceComparatorOptions.all().copy(compareLvSimplification = false),
            optionsStopCompare = true
        )
    }

    @Test
    internal fun compareMeter() {
        compareEndDevice { Meter(mRID = it) }
    }

    @Test
    internal fun compareUsagePoint() {
        compareIdentifiedObject { UsagePoint(it) }

        comparatorValidator.validateProperty(UsagePoint::usagePointLocation, { UsagePoint(it) }, { Location("l1") }, { Location("l2") })
        comparatorValidator.validateProperty(UsagePoint::isVirtual, { UsagePoint(it) }, { false }, { true })
        comparatorValidator.validateProperty(UsagePoint::connectionCategory, { UsagePoint(it) }, { "first" }, { "second" })
        comparatorValidator.validateCollection(
            UsagePoint::endDevices,
            UsagePoint::addEndDevice,
            { UsagePoint(it) },
            { Meter("m1") },
            { Meter("m2") },
            NetworkServiceComparatorOptions.all().copy(compareLvSimplification = false),
            optionsStopCompare = true
        )

        comparatorValidator.validateCollection(
            UsagePoint::equipment,
            UsagePoint::addEquipment,
            { UsagePoint(it) },
            { Junction("j1") },
            { Junction("j2") },
            NetworkServiceComparatorOptions.all().copy(compareLvSimplification = false),
            optionsStopCompare = true
        )
    }

    /************ IEC61968 OPERATIONS ************/

    @Test
    internal fun compareOperationalRestriction() {
        compareDocument { OperationalRestriction(it) }

        comparatorValidator.validateCollection(
            OperationalRestriction::equipment,
            OperationalRestriction::addEquipment,
            { OperationalRestriction(it) },
            { Junction("j1") },
            { Junction("j2") })
    }

    /************ IEC61970 BASE AUXILIARY EQUIPMENT ************/

    private fun compareAuxiliaryEquipment(createAuxiliaryEquipment: (String) -> AuxiliaryEquipment) {
        compareEquipment(createAuxiliaryEquipment)

        comparatorValidator.validateProperty(
            AuxiliaryEquipment::terminal,
            createAuxiliaryEquipment,
            { Terminal("t1") },
            { Terminal("t2") },
            NetworkServiceComparatorOptions.all().copy(compareTerminals = false),
            optionsStopCompare = true
        )
    }

    @Test
    internal fun compareFaultIndicator() {
        compareAuxiliaryEquipment { FaultIndicator(it) }
    }

    /************ IEC61970 BASE CORE ************/

    private fun compareAcDcTerminal(createAcDcTerminal: (String) -> AcDcTerminal) {
        compareIdentifiedObject(createAcDcTerminal)
    }

    @Test
    internal fun compareBaseVoltage() {
        compareIdentifiedObject { BaseVoltage(it) }

        comparatorValidator.validateProperty(BaseVoltage::nominalVoltage, { BaseVoltage(it) }, { 1 }, { 2 })
    }

    private fun compareConductingEquipment(createConductingEquipment: (String) -> ConductingEquipment) {
        compareEquipment(createConductingEquipment)

        comparatorValidator.validateProperty(ConductingEquipment::baseVoltage, createConductingEquipment, { BaseVoltage("b1") }, { BaseVoltage("b2") })
        comparatorValidator.validateIndexedCollection(
            ConductingEquipment::terminals, ConductingEquipment::addTerminal,
            createConductingEquipment,
            { Terminal(mRID = "1").apply { conductingEquipment = it } },
            { Terminal(mRID = "2").apply { conductingEquipment = it } },
            Terminal::conductingEquipment.setter,
            NetworkServiceComparatorOptions.all().copy(compareTerminals = false), optionsStopCompare = true
        )
    }

    @Test
    internal fun compareConnectivityNode() {
        compareIdentifiedObject { ConnectivityNode(it) }

        comparatorValidator.validateCollection(
            ConnectivityNode::terminals,
            ConnectivityNode::addTerminal,
            { ConnectivityNode(it) },
            { Terminal("1") },
            { Terminal("2") })
    }

    private fun compareConnectivityNodeContainer(createConnectivityNodeContainer: (String) -> ConnectivityNodeContainer) {
        comparePowerSystemResource(createConnectivityNodeContainer)
    }

    private fun compareEquipment(createEquipment: (String) -> Equipment) {
        comparePowerSystemResource(createEquipment)

        comparatorValidator.validateProperty(Equipment::inService, createEquipment, { true }, { false })
        comparatorValidator.validateProperty(Equipment::normallyInService, createEquipment, { true }, { false })
        comparatorValidator.validateCollection(Equipment::containers, Equipment::addContainer, createEquipment, { Site("s1") }, { Site("s2") })

        comparatorValidator.validateCollection(Equipment::usagePoints, Equipment::addUsagePoint, createEquipment, { UsagePoint("u1") }, { UsagePoint("u2") })

        comparatorValidator.validateCollection(
            Equipment::operationalRestrictions,
            Equipment::addOperationalRestriction,
            createEquipment,
            { OperationalRestriction("o1") },
            { OperationalRestriction("o2") })

        comparatorValidator.validateCollection(Equipment::currentFeeders, Equipment::addCurrentFeeder, createEquipment, { Feeder("f1") }, { Feeder("f2") })
    }

    private fun compareEquipmentContainer(createEquipmentContainer: (String) -> EquipmentContainer) {
        compareConnectivityNodeContainer(createEquipmentContainer)

        comparatorValidator.validateCollection(
            EquipmentContainer::equipment,
            EquipmentContainer::addEquipment,
            createEquipmentContainer,
            { Junction("j1") },
            { Junction("j2") })
    }

    @Test
    internal fun compareFeeder() {
        compareEquipmentContainer { Feeder(it) }

        comparatorValidator.validateProperty(Feeder::normalHeadTerminal, { Feeder(it) }, { Terminal("t1") }, { Terminal("t2") })
        comparatorValidator.validateProperty(Feeder::normalEnergizingSubstation, { Feeder(it) }, { Substation("s1") }, { Substation("s2") })
        comparatorValidator.validateCollection(Feeder::currentEquipment, Feeder::addCurrentEquipment, { Feeder(it) }, { Junction("j1") }, { Junction("j2") })
    }

    @Test
    internal fun compareGeographicalRegion() {
        compareIdentifiedObject { GeographicalRegion(it) }

        comparatorValidator.validateCollection(
            GeographicalRegion::subGeographicalRegions,
            GeographicalRegion::addSubGeographicalRegion,
            { GeographicalRegion(it) },
            { SubGeographicalRegion("sg1") },
            { SubGeographicalRegion("sg2") })
    }

    private fun comparePowerSystemResource(createPowerSystemResource: (String) -> PowerSystemResource) {
        compareIdentifiedObject(createPowerSystemResource)

        comparatorValidator.validateProperty(PowerSystemResource::location, createPowerSystemResource, { Location("l1") }, { Location("l2") })
        comparatorValidator.validateProperty(PowerSystemResource::numControls, createPowerSystemResource, { 1 }, { 2 })
    }

    @Test
    internal fun compareSite() {
        compareEquipmentContainer { Site(it) }
    }

    @Test
    internal fun compareSubGeographicalRegion() {
        compareIdentifiedObject { SubGeographicalRegion(it) }

        comparatorValidator.validateProperty(
            SubGeographicalRegion::geographicalRegion,
            { SubGeographicalRegion(it) },
            { GeographicalRegion("g1") },
            { GeographicalRegion("g2") })

        comparatorValidator.validateCollection(
            SubGeographicalRegion::substations,
            SubGeographicalRegion::addSubstation,
            { SubGeographicalRegion(it) },
            { Substation("s1") },
            { Substation("s2") })
    }

    @Test
    internal fun compareSubstation() {
        compareEquipmentContainer { Substation(it) }

        comparatorValidator.validateProperty(
            Substation::subGeographicalRegion,
            { Substation(it) },
            { SubGeographicalRegion("sg1") },
            { SubGeographicalRegion("sg2") })

        comparatorValidator.validateCollection(Substation::feeders, Substation::addFeeder, { Substation(it) }, { Feeder("f1") }, { Feeder("f2") })
    }

    @Test
    internal fun compareTerminal() {
        compareAcDcTerminal { Terminal(it) }

        comparatorValidator.validateProperty(Terminal::phases, { Terminal(it) }, { PhaseCode.ABC }, { PhaseCode.ABCN })

        comparatorValidator.validateValProperty(
            Terminal::connectivityNode, { Terminal(it) },
            { terminal, _ -> terminal.connect(ConnectivityNode("c1")) }, { terminal, _ -> terminal.connect(ConnectivityNode("c2")) })

        comparatorValidator.validateProperty(Terminal::conductingEquipment, { Terminal(it) }, { Junction("j1") }, { Junction("j2") })

        val createAbcnTerminal = { id: String -> Terminal(id).apply { phases = PhaseCode.ABCN } }
        val initTracedPhases = { _: Terminal, tracedPhases: TracedPhases ->
            PhaseCode.ABCN.singlePhases().forEach {
                tracedPhases.setNormal(it, PhaseDirection.BOTH, it)
                tracedPhases.setCurrent(it, PhaseDirection.BOTH, it)
            }
        }

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.B, PhaseDirection.BOTH, SinglePhaseKind.A) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.B, PhaseDirection.BOTH, SinglePhaseKind.A) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.A, PhaseDirection.IN, SinglePhaseKind.A) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.B, PhaseDirection.BOTH, SinglePhaseKind.A) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.A, PhaseDirection.OUT, SinglePhaseKind.A) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.B, PhaseDirection.IN, SinglePhaseKind.B) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.C, PhaseDirection.BOTH, SinglePhaseKind.B) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.B, PhaseDirection.OUT, SinglePhaseKind.B) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.C) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.C, PhaseDirection.OUT, SinglePhaseKind.C) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.N, PhaseDirection.BOTH, SinglePhaseKind.C) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.C, PhaseDirection.IN, SinglePhaseKind.C) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.A, PhaseDirection.BOTH, SinglePhaseKind.N) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setNormal(SinglePhaseKind.N, PhaseDirection.OUT, SinglePhaseKind.N) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.A, PhaseDirection.BOTH, SinglePhaseKind.N) })

        comparatorValidator.validateValProperty(
            Terminal::tracedPhases, createAbcnTerminal,
            initTracedPhases, { _, tracedPhases -> tracedPhases.setCurrent(SinglePhaseKind.N, PhaseDirection.IN, SinglePhaseKind.N) })
    }

    /************ IEC61970 BASE EQUIVALENTS ************/

    @Test
    internal fun compareEquivalentBranch() {
        compareEquivalentEquipment { EquivalentBranch(it) }

        comparatorValidator.validateProperty(EquivalentBranch::negativeR12, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::negativeR21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::negativeX12, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::negativeX21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::positiveR12, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::positiveR21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::positiveX12, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::positiveX21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::r, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::r21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::x, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::x21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::zeroR12, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::zeroR21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::zeroX12, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EquivalentBranch::zeroX21, { EquivalentBranch(it) }, { 1.0 }, { 2.0 })
    }

    private fun compareEquivalentEquipment(createEquivalentEquipment: (String) -> EquivalentEquipment) {
        compareConductingEquipment(createEquivalentEquipment)
    }

    /************ IEC61970 BASE MEAS ************/

    @Test
    internal fun compareAccumulator() {
        compareMeasurement { Accumulator(it) }
    }

    @Test
    internal fun compareAnalog() {
        compareMeasurement { Analog(it) }
        comparatorValidator.validateProperty(Analog::positiveFlowIn, { Analog(it) }, { true }, { false })
    }

    @Test
    internal fun compareDiscrete() {
        compareMeasurement { Discrete(it) }
    }

    private fun compareMeasurement(createIdObj: (String) -> Measurement) {
        compareIdentifiedObject { createIdObj(it) }

        comparatorValidator.validateProperty(Measurement::powerSystemResourceMRID, { createIdObj(it) }, { "psr1" }, { "psr2" })
        comparatorValidator.validateProperty(Measurement::terminalMRID, { createIdObj(it) }, { "terminal1" }, { "terminal2" })
        comparatorValidator.validateProperty(Measurement::remoteSource, { createIdObj(it) }, { RemoteSource("rs1") }, { RemoteSource("rs2") })
        comparatorValidator.validateProperty(Measurement::phases, { createIdObj(it) }, { PhaseCode.ABCN }, { PhaseCode.ABC })
        comparatorValidator.validateProperty(Measurement::unitSymbol, { createIdObj(it) }, { UnitSymbol.HENRYS }, { UnitSymbol.HOURS })
    }

    /************ IEC61970 BASE SCADA ************/

    @Test
    internal fun compareRemoteControl() {
        compareRemotePoint { RemoteControl(it) }

        comparatorValidator.validateProperty(RemoteControl::control, { RemoteControl(it) }, { Control("c1") }, { Control("c2") })
    }

    private fun compareRemotePoint(createIdObj: (String) -> RemotePoint) {
        compareIdentifiedObject { createIdObj(it) }
    }

    @Test
    internal fun compareRemoteSource() {
        compareRemotePoint { RemoteSource(it) }

        comparatorValidator.validateProperty(
            RemoteSource::measurement,
            { RemoteSource(it) },
            { object : Measurement("m1") {} },
            { object : Measurement("m2") {} })
    }

    /************ IEC61970 BASE WIRES GENERATION PRODUCTION ************/

    @Test
    internal fun compareBatteryUnit() {
        comparePowerElectronicsUnit { BatteryUnit(it) }

        comparatorValidator.validateProperty(BatteryUnit::batteryState, { BatteryUnit(it) }, { BatteryStateKind.charging }, { BatteryStateKind.discharging })
        comparatorValidator.validateProperty(BatteryUnit::ratedE, { BatteryUnit(it) }, { 1L }, { 2L })
        comparatorValidator.validateProperty(BatteryUnit::storedE, { BatteryUnit(it) }, { 1L }, { 2L })
    }

    @Test
    internal fun comparePhotoVoltaicUnit() {
        comparePowerElectronicsUnit { PhotoVoltaicUnit(it) }
    }

    private fun comparePowerElectronicsUnit(createPowerElectronicsUnit: (String) -> PowerElectronicsUnit) {
        compareEquipment(createPowerElectronicsUnit)

        comparatorValidator.validateProperty(
            PowerElectronicsUnit::powerElectronicsConnection,
            createPowerElectronicsUnit,
            { PowerElectronicsConnection("pec1") },
            { PowerElectronicsConnection("pec2") })
        comparatorValidator.validateProperty(PowerElectronicsUnit::maxP, createPowerElectronicsUnit, { 1 }, { 2 })
        comparatorValidator.validateProperty(PowerElectronicsUnit::minP, createPowerElectronicsUnit, { 1 }, { 2 })
    }

    @Test
    internal fun comparePowerElectronicsWindUnit() {
        comparePowerElectronicsUnit { PowerElectronicsWindUnit(it) }
    }

    /************ IEC61970 BASE WIRES ************/

    @Test
    internal fun compareAcLineSegment() {
        compareConductor { AcLineSegment(it) }

        comparatorValidator.validateProperty(
            AcLineSegment::perLengthSequenceImpedance,
            { AcLineSegment(it) },
            { PerLengthSequenceImpedance("p1") },
            { PerLengthSequenceImpedance("p2") })
    }

    @Test
    internal fun compareBreaker() {
        compareProtectedSwitch { Breaker(it) }
    }

    @Test
    internal fun compareBusbarSection() {
        compareConnector { BusbarSection(it) }
    }

    private fun compareConductor(createConductor: (String) -> Conductor) {
        compareConductingEquipment(createConductor)

        comparatorValidator.validateProperty(Conductor::length, createConductor, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(Conductor::assetInfo, createConductor, { CableInfo("c1") }, { CableInfo("c2") })
    }

    private fun compareConnector(createConnector: (String) -> Connector) {
        compareConductingEquipment(createConnector)
    }

    @Test
    internal fun compareDisconnector() {
        compareSwitch { Disconnector(it) }
    }

    private fun compareEnergyConnection(createEnergyConnection: (String) -> EnergyConnection) {
        compareConductingEquipment(createEnergyConnection)
    }

    @Test
    internal fun compareEnergyConsumer() {
        compareEnergyConnection { EnergyConsumer(it) }

        comparatorValidator.validateProperty(EnergyConsumer::customerCount, { EnergyConsumer(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(EnergyConsumer::grounded, { EnergyConsumer(it) }, { true }, { false })
        comparatorValidator.validateProperty(EnergyConsumer::p, { EnergyConsumer(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergyConsumer::pFixed, { EnergyConsumer(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(
            EnergyConsumer::phaseConnection,
            { EnergyConsumer(it) },
            { PhaseShuntConnectionKind.I },
            { PhaseShuntConnectionKind.D })
        comparatorValidator.validateProperty(EnergyConsumer::q, { EnergyConsumer(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergyConsumer::qFixed, { EnergyConsumer(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateCollection(
            EnergyConsumer::phases,
            EnergyConsumer::addPhase,
            { EnergyConsumer(it) },
            { EnergyConsumerPhase("ecp1") },
            { EnergyConsumerPhase("ecp2") })
    }

    @Test
    internal fun compareEnergyConsumerPhase() {
        comparePowerSystemResource { EnergyConsumerPhase(it) }

        comparatorValidator.validateProperty(
            EnergyConsumerPhase::energyConsumer,
            { EnergyConsumerPhase(it) },
            { EnergyConsumer("ec1") },
            { EnergyConsumer("ec2") })

        comparatorValidator.validateProperty(EnergyConsumerPhase::phase, { EnergyConsumerPhase(it) }, { SinglePhaseKind.A }, { SinglePhaseKind.B })
        comparatorValidator.validateProperty(EnergyConsumerPhase::p, { EnergyConsumerPhase(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergyConsumerPhase::pFixed, { EnergyConsumerPhase(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergyConsumerPhase::q, { EnergyConsumerPhase(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergyConsumerPhase::qFixed, { EnergyConsumerPhase(it) }, { 1.0 }, { 2.0 })
    }

    @Test
    internal fun compareEnergySource() {
        compareEnergyConnection { EnergySource(it) }

        comparatorValidator.validateProperty(EnergySource::activePower, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::reactivePower, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::voltageAngle, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::voltageMagnitude, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::pMax, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::pMin, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::r, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::r0, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::rn, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::x, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::x0, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(EnergySource::xn, { EnergySource(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateCollection(
            EnergySource::phases,
            EnergySource::addPhase,
            { EnergySource(it) },
            { EnergySourcePhase("ecp1") },
            { EnergySourcePhase("ecp2") })
    }

    @Test
    internal fun compareEnergySourcePhase() {
        comparePowerSystemResource { EnergySourcePhase(it) }

        comparatorValidator.validateProperty(EnergySourcePhase::phase, { EnergySourcePhase(it) }, { SinglePhaseKind.A }, { SinglePhaseKind.B })
        comparatorValidator.validateProperty(EnergySourcePhase::energySource, { EnergySourcePhase(it) }, { EnergySource("es1") }, { EnergySource("es2") })
    }

    @Test
    internal fun compareFuse() {
        compareSwitch { Fuse(it) }
    }

    @Test
    internal fun compareJumper() {
        compareSwitch { Jumper(it) }
    }

    @Test
    internal fun compareJunction() {
        compareConnector { Junction(it) }
    }

    private fun compareLine(createLine: (String) -> Line) {
        compareEquipmentContainer(createLine)
    }

    @Test
    internal fun compareLinearShuntCompensator() {
        compareShuntCompensator { LinearShuntCompensator(it) }


        comparatorValidator.validateProperty(LinearShuntCompensator::b0PerSection, { LinearShuntCompensator(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(LinearShuntCompensator::bPerSection, { LinearShuntCompensator(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(LinearShuntCompensator::g0PerSection, { LinearShuntCompensator(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(LinearShuntCompensator::gPerSection, { LinearShuntCompensator(it) }, { 1.0 }, { 2.0 })
    }

    private fun comparePerLengthImpedance(createPerLengthImpedance: (String) -> PerLengthImpedance) {
        comparePerLengthLineParameter(createPerLengthImpedance)
    }

    private fun comparePerLengthLineParameter(createPerLengthLineParameter: (String) -> PerLengthLineParameter) {
        compareIdentifiedObject(createPerLengthLineParameter)
    }

    @Test
    internal fun comparePerLengthSequenceImpedance() {
        comparePerLengthImpedance { PerLengthSequenceImpedance(it) }

        comparatorValidator.validateProperty(PerLengthSequenceImpedance::r, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PerLengthSequenceImpedance::x, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PerLengthSequenceImpedance::bch, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PerLengthSequenceImpedance::gch, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PerLengthSequenceImpedance::r0, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PerLengthSequenceImpedance::x0, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PerLengthSequenceImpedance::b0ch, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PerLengthSequenceImpedance::g0ch, { PerLengthSequenceImpedance(it) }, { 1.0 }, { 2.0 })
    }

    @Test
    internal fun comparePowerElectronicsConnection() {
        compareRegulatingCondEq { PowerElectronicsConnection(it) }

        comparatorValidator.validateCollection(
            PowerElectronicsConnection::phases,
            PowerElectronicsConnection::addPhase,
            { PowerElectronicsConnection(it) },
            { PowerElectronicsConnectionPhase("pecp1") },
            { PowerElectronicsConnectionPhase("pecp2") })
        comparatorValidator.validateCollection(
            PowerElectronicsConnection::units,
            PowerElectronicsConnection::addUnit,
            { PowerElectronicsConnection(it) },
            { object : PowerElectronicsUnit("peu1") {} },
            { object : PowerElectronicsUnit("peu2") {} })

        comparatorValidator.validateProperty(PowerElectronicsConnection::maxIFault, { PowerElectronicsConnection(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(PowerElectronicsConnection::maxQ, { PowerElectronicsConnection(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerElectronicsConnection::minQ, { PowerElectronicsConnection(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerElectronicsConnection::p, { PowerElectronicsConnection(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerElectronicsConnection::q, { PowerElectronicsConnection(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerElectronicsConnection::ratedS, { PowerElectronicsConnection(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(PowerElectronicsConnection::ratedU, { PowerElectronicsConnection(it) }, { 1 }, { 2 })
    }

    @Test
    internal fun comparePowerElectronicsConnectionPhase() {
        comparePowerSystemResource { PowerElectronicsConnectionPhase(it) }

        comparatorValidator.validateProperty(
            PowerElectronicsConnectionPhase::phase,
            { PowerElectronicsConnectionPhase(it) },
            { SinglePhaseKind.A },
            { SinglePhaseKind.B })
        comparatorValidator.validateProperty(
            PowerElectronicsConnectionPhase::powerElectronicsConnection,
            { PowerElectronicsConnectionPhase(it) },
            { PowerElectronicsConnection("pec1") },
            { PowerElectronicsConnection("pec2") })
    }

    @Test
    internal fun comparePowerTransformer() {
        compareConductingEquipment { PowerTransformer(it) }

        comparatorValidator.validateProperty(PowerTransformer::vectorGroup, { PowerTransformer(it) }, { VectorGroup.DYN11 }, { VectorGroup.D0 })
        comparatorValidator.validateProperty(PowerTransformer::transformerUtilisation, { PowerTransformer(it) }, { 0.1 }, { 0.9 })

        comparatorValidator.validateIndexedCollection(
            PowerTransformer::ends, PowerTransformer::addEnd, { PowerTransformer(it) },
            { PowerTransformerEnd(mRID = "pte1") }, { PowerTransformerEnd(mRID = "pte2") }, PowerTransformerEnd::powerTransformer.setter
        )
    }

    @Test
    internal fun comparePowerTransformerEnd() {
        compareTransformerEnd { PowerTransformerEnd(it) }

        comparatorValidator.validateProperty(
            PowerTransformerEnd::powerTransformer,
            { PowerTransformerEnd(it) },
            { PowerTransformer("pt1") },
            { PowerTransformer("pt2") })
        comparatorValidator.validateProperty(PowerTransformerEnd::b, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerTransformerEnd::b0, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerTransformerEnd::connectionKind, { PowerTransformerEnd(it) }, { WindingConnection.A }, { WindingConnection.D })
        comparatorValidator.validateProperty(PowerTransformerEnd::g, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerTransformerEnd::g0, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerTransformerEnd::phaseAngleClock, { PowerTransformerEnd(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(PowerTransformerEnd::r, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerTransformerEnd::r0, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerTransformerEnd::ratedS, { PowerTransformerEnd(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(PowerTransformerEnd::ratedU, { PowerTransformerEnd(it) }, { 1 }, { 2 })
        comparatorValidator.validateProperty(PowerTransformerEnd::x, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(PowerTransformerEnd::x0, { PowerTransformerEnd(it) }, { 1.0 }, { 2.0 })

        comparatorValidator.validateProperty(PowerTransformerEnd::r, { PowerTransformerEnd(it) }, { 1.0 }, { Double.NaN })
        comparatorValidator.validateProperty(PowerTransformerEnd::r0, { PowerTransformerEnd(it) }, { 1.0 }, { Double.NaN })
        comparatorValidator.validateProperty(PowerTransformerEnd::x, { PowerTransformerEnd(it) }, { 1.0 }, { Double.NaN })
        comparatorValidator.validateProperty(PowerTransformerEnd::x0, { PowerTransformerEnd(it) }, { 1.0 }, { Double.NaN })
    }

    private fun compareProtectedSwitch(createProtectedSwitch: (String) -> ProtectedSwitch) {
        compareSwitch(createProtectedSwitch)
    }

    @Test
    internal fun compareRatioTapChanger() {
        compareTapChanger { RatioTapChanger(it) }

        comparatorValidator.validateProperty(
            RatioTapChanger::transformerEnd,
            { RatioTapChanger(it) },
            { PowerTransformerEnd("pte1") },
            { PowerTransformerEnd("pte2") })

        comparatorValidator.validateProperty(RatioTapChanger::stepVoltageIncrement, { RatioTapChanger(it) }, { 1.0 }, { 2.0 })
    }

    @Test
    internal fun compareRecloser() {
        compareProtectedSwitch { Recloser(it) }
    }

    private fun compareRegulatingCondEq(createRegulatingCondEq: (String) -> RegulatingCondEq) {
        compareEnergyConnection(createRegulatingCondEq)

        comparatorValidator.validateProperty(RegulatingCondEq::controlEnabled, createRegulatingCondEq, { false }, { true })
    }

    private fun compareShuntCompensator(createShuntCompensator: (String) -> ShuntCompensator) {
        compareRegulatingCondEq(createShuntCompensator)

        comparatorValidator.validateProperty(ShuntCompensator::grounded, createShuntCompensator, { false }, { true })
        comparatorValidator.validateProperty(ShuntCompensator::nomU, createShuntCompensator, { 1 }, { 2 })
        comparatorValidator.validateProperty(
            ShuntCompensator::phaseConnection,
            createShuntCompensator,
            { PhaseShuntConnectionKind.D },
            { PhaseShuntConnectionKind.G })
        comparatorValidator.validateProperty(ShuntCompensator::sections, createShuntCompensator, { 1.0 }, { 2.0 })
    }

    private fun compareSwitch(createSwitch: (String) -> Switch) {
        compareConductingEquipment(createSwitch)

        val closedSwitch = createSwitch("mRID").apply { setNormallyOpen(false); setOpen(true) }
        val openSwitch = createSwitch("mRID").apply { setNormallyOpen(true); setOpen(false) }

        val difference = ObjectDifference(closedSwitch, openSwitch).apply {
            differences["isNormallyOpen"] = ValueDifference(
                PhaseCode.ABCN.singlePhases().associateWith { false },
                PhaseCode.ABCN.singlePhases().associateWith { true })

            differences["isOpen"] = ValueDifference(
                PhaseCode.ABCN.singlePhases().associateWith { true },
                PhaseCode.ABCN.singlePhases().associateWith { false })
        }

        comparatorValidator.validateCompare(closedSwitch, openSwitch, expectModification = difference)
    }

    private fun compareTapChanger(createTapChanger: (String) -> TapChanger) {
        comparePowerSystemResource(createTapChanger)

        comparatorValidator.validateProperty(TapChanger::controlEnabled, createTapChanger, { true }, { false })
        comparatorValidator.validateProperty(TapChanger::highStep, createTapChanger, { 1 }, { 2 })
        comparatorValidator.validateProperty(TapChanger::lowStep, { createTapChanger(it).apply { highStep = 10 } }, { 0 }, { 1 })
        comparatorValidator.validateProperty(TapChanger::neutralStep, { createTapChanger(it).apply { highStep = 10 } }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TapChanger::neutralU, createTapChanger, { 1 }, { 2 })
        comparatorValidator.validateProperty(TapChanger::normalStep, { createTapChanger(it).apply { highStep = 10 } }, { 1 }, { 2 })
        comparatorValidator.validateProperty(TapChanger::step, { createTapChanger(it).apply { highStep = 10 } }, { 1.0 }, { 2.0 })
    }

    private fun compareTransformerEnd(createTransformerEnd: (String) -> TransformerEnd) {
        compareIdentifiedObject(createTransformerEnd)

        comparatorValidator.validateProperty(TransformerEnd::grounded, createTransformerEnd, { true }, { false })
        comparatorValidator.validateProperty(TransformerEnd::rGround, createTransformerEnd, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(TransformerEnd::xGround, createTransformerEnd, { 1.0 }, { 2.0 })
        comparatorValidator.validateProperty(TransformerEnd::baseVoltage, createTransformerEnd, { BaseVoltage("bv1") }, { BaseVoltage("b21") })
        comparatorValidator.validateProperty(TransformerEnd::ratioTapChanger, createTransformerEnd, { RatioTapChanger("rtc1") }, { RatioTapChanger("rtc2") })
        comparatorValidator.validateProperty(TransformerEnd::terminal, createTransformerEnd, { Terminal("t1") }, { Terminal("t2") })
        comparatorValidator.validateProperty(
            TransformerEnd::starImpedance,
            createTransformerEnd,
            { TransformerStarImpedance("tsi1") },
            { TransformerStarImpedance("tsi2") })
    }

    @Test
    internal fun compareCircuit() {
        compareLine { Circuit(it) }

        comparatorValidator.validateProperty(Circuit::loop, { Circuit(it) }, { Loop("l1") }, { Loop("l2") })

        comparatorValidator.validateCollection(Circuit::endTerminals, Circuit::addEndTerminal, { Circuit(it) }, { Terminal("t1") }, { Terminal("t2") })

        comparatorValidator.validateCollection(Circuit::endSubstations, Circuit::addEndSubstation, { Circuit(it) }, { Substation("s1") }, { Substation("s2") })
    }

    @Test
    internal fun compareLoop() {
        compareIdentifiedObject { Loop(it) }

        comparatorValidator.validateCollection(Loop::circuits, Loop::addCircuit, { Loop(it) }, { Circuit("c1") }, { Circuit("c2") })
        comparatorValidator.validateCollection(Loop::substations, Loop::addSubstation, { Loop(it) }, { Substation("s1") }, { Substation("s2") })
        comparatorValidator.validateCollection(
            Loop::energizingSubstations,
            Loop::addEnergizingSubstation,
            { Loop(it) },
            { Substation("s1") },
            { Substation("s2") })
    }

}
