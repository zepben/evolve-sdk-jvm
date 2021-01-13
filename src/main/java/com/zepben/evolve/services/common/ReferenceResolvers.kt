/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.services.common

import com.zepben.evolve.cim.iec61968.assetinfo.PowerTransformerInfo
import com.zepben.evolve.cim.iec61968.assetinfo.WireInfo
import com.zepben.evolve.cim.iec61968.assets.Asset
import com.zepben.evolve.cim.iec61968.assets.AssetOrganisationRole
import com.zepben.evolve.cim.iec61968.assets.Pole
import com.zepben.evolve.cim.iec61968.assets.Streetlight
import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.common.Organisation
import com.zepben.evolve.cim.iec61968.common.OrganisationRole
import com.zepben.evolve.cim.iec61968.customers.Customer
import com.zepben.evolve.cim.iec61968.customers.CustomerAgreement
import com.zepben.evolve.cim.iec61968.customers.PricingStructure
import com.zepben.evolve.cim.iec61968.customers.Tariff
import com.zepben.evolve.cim.iec61968.metering.EndDevice
import com.zepben.evolve.cim.iec61968.metering.UsagePoint
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.AuxiliaryEquipment
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.diagramlayout.Diagram
import com.zepben.evolve.cim.iec61970.base.diagramlayout.DiagramObject
import com.zepben.evolve.cim.iec61970.base.meas.Control
import com.zepben.evolve.cim.iec61970.base.meas.Measurement
import com.zepben.evolve.cim.iec61970.base.scada.RemoteControl
import com.zepben.evolve.cim.iec61970.base.scada.RemoteSource
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Loop
import kotlin.reflect.KClass


internal object AcLineSegmentToPerLengthSequenceImpedanceResolver : ReferenceResolver<AcLineSegment, PerLengthSequenceImpedance> by KReferenceResolver(
    AcLineSegment::class, PerLengthSequenceImpedance::class, AcLineSegment::perLengthSequenceImpedance.setter
)

internal object AssetToAssetOrganisationRoleResolver : ReferenceResolver<Asset, AssetOrganisationRole> by KReferenceResolver(
    Asset::class, AssetOrganisationRole::class, Asset::addOrganisationRole
)

internal object AssetToLocationResolver : ReferenceResolver<Asset, Location> by KReferenceResolver(
    Asset::class, Location::class, Asset::location.setter
)

internal object PoleToStreetlightResolver : ReferenceResolver<Pole, Streetlight> by KReferenceResolver(
    Pole::class, Streetlight::class, Pole::addStreetlight
)

internal object StreetlightToPoleResolver : ReferenceResolver<Streetlight, Pole> by KReferenceResolver(
    Streetlight::class, Pole::class, Streetlight::pole.setter
)

internal object AuxiliaryEquipmentToTerminalResolver : ReferenceResolver<AuxiliaryEquipment, Terminal> by KReferenceResolver(
    AuxiliaryEquipment::class, Terminal::class, AuxiliaryEquipment::terminal.setter
)

internal object ConductingEquipmentToBaseVoltageResolver : ReferenceResolver<ConductingEquipment, BaseVoltage> by KReferenceResolver(
    ConductingEquipment::class, BaseVoltage::class, ConductingEquipment::baseVoltage.setter
)

internal object ConductingEquipmentToTerminalsResolver : ReferenceResolver<ConductingEquipment, Terminal> by KReferenceResolver(
    ConductingEquipment::class,
    Terminal::class,
    { ce, t ->
        t.conductingEquipment = ce
        ce.addTerminal(t)
    })

internal object ConductorToWireInfoResolver : ReferenceResolver<Conductor, WireInfo> by KReferenceResolver(
    Conductor::class, WireInfo::class, Conductor::assetInfo.setter
)

internal object PowerTransformerToPowerTransformerInfoResolver : ReferenceResolver<PowerTransformer, PowerTransformerInfo> by KReferenceResolver(
    PowerTransformer::class, PowerTransformerInfo::class, PowerTransformer::assetInfo.setter
)

internal object ConnectivityNodeToTerminalResolver : ReferenceResolver<ConnectivityNode, Terminal> by KReferenceResolver(
    ConnectivityNode::class, Terminal::class, ConnectivityNode::addTerminal
)

internal object ControlToRemoteControlResolver : ReferenceResolver<Control, RemoteControl> by KReferenceResolver(
    Control::class, RemoteControl::class, Control::remoteControl.setter
)

internal object CustomerToCustomerAgreementResolver : ReferenceResolver<Customer, CustomerAgreement> by KReferenceResolver(
    Customer::class, CustomerAgreement::class, Customer::addAgreement
)

internal object CustomerAgreementToCustomerResolver : ReferenceResolver<CustomerAgreement, Customer> by KReferenceResolver(
    CustomerAgreement::class, Customer::class, CustomerAgreement::customer.setter
)

internal object CustomerAgreementToPricingStructureResolver : ReferenceResolver<CustomerAgreement, PricingStructure> by KReferenceResolver(
    CustomerAgreement::class, PricingStructure::class, CustomerAgreement::addPricingStructure
)

internal object DiagramToDiagramObjectResolver : ReferenceResolver<Diagram, DiagramObject> by KReferenceResolver(
    Diagram::class, DiagramObject::class,
    { diagram, diagramObject ->
        diagramObject.diagram = diagram
        diagram.addDiagramObject(diagramObject)
    })

internal object DiagramObjectToDiagramResolver : ReferenceResolver<DiagramObject, Diagram> by KReferenceResolver(
    DiagramObject::class, Diagram::class, DiagramObject::diagram.setter
)

internal object EndDeviceToUsagePointResolver : ReferenceResolver<EndDevice, UsagePoint> by KReferenceResolver(
    EndDevice::class, UsagePoint::class, EndDevice::addUsagePoint
)

internal object EndDeviceToServiceLocationResolver : ReferenceResolver<EndDevice, Location> by KReferenceResolver(
    EndDevice::class, Location::class, EndDevice::serviceLocation.setter
)

internal object EnergyConsumerToEnergyConsumerPhaseResolver : ReferenceResolver<EnergyConsumer, EnergyConsumerPhase> by KReferenceResolver(
    EnergyConsumer::class, EnergyConsumerPhase::class, EnergyConsumer::addPhase
)

internal object EnergyConsumerPhaseToEnergyConsumerResolver : ReferenceResolver<EnergyConsumerPhase, EnergyConsumer> by KReferenceResolver(
    EnergyConsumerPhase::class, EnergyConsumer::class, EnergyConsumerPhase::energyConsumer.setter
)

internal object EnergySourceToEnergySourcePhaseResolver : ReferenceResolver<EnergySource, EnergySourcePhase> by KReferenceResolver(
    EnergySource::class, EnergySourcePhase::class, EnergySource::addPhase
)

internal object EnergySourcePhaseToEnergySourceResolver : ReferenceResolver<EnergySourcePhase, EnergySource> by KReferenceResolver(
    EnergySourcePhase::class, EnergySource::class, EnergySourcePhase::energySource.setter
)

internal object EquipmentToCurrentFeedersResolver : ReferenceResolver<Equipment, Feeder> by KReferenceResolver(
    Equipment::class, Feeder::class, Equipment::addCurrentFeeder
)

internal object EquipmentToEquipmentContainerResolver : ReferenceResolver<Equipment, EquipmentContainer> by KReferenceResolver(
    Equipment::class, EquipmentContainer::class, Equipment::addContainer
)

internal object EquipmentToOperationalRestrictionResolver : ReferenceResolver<Equipment, OperationalRestriction> by KReferenceResolver(
    Equipment::class, OperationalRestriction::class, Equipment::addOperationalRestriction
)

internal object EquipmentToUsagePointResolver : ReferenceResolver<Equipment, UsagePoint> by KReferenceResolver(
    Equipment::class, UsagePoint::class, Equipment::addUsagePoint
)

internal object EquipmentContainerToEquipmentResolver : ReferenceResolver<EquipmentContainer, Equipment> by KReferenceResolver(
    EquipmentContainer::class, Equipment::class, EquipmentContainer::addEquipment
)

internal object CurrentFeederToEquipmentResolver : ReferenceResolver<Feeder, Equipment> by KReferenceResolver(
    Feeder::class, Equipment::class, Feeder::addCurrentEquipment
)

internal object FeederToNormalEnergizingSubstationResolver : ReferenceResolver<Feeder, Substation> by KReferenceResolver(
    Feeder::class, Substation::class, Feeder::normalEnergizingSubstation.setter
)

internal object FeederToNormalHeadTerminalResolver : ReferenceResolver<Feeder, Terminal> by KReferenceResolver(
    Feeder::class, Terminal::class, Feeder::normalHeadTerminal.setter
)

internal object GeographicalRegionToSubGeographicalRegionResolver : ReferenceResolver<GeographicalRegion, SubGeographicalRegion> by KReferenceResolver(
    GeographicalRegion::class, SubGeographicalRegion::class, GeographicalRegion::addSubGeographicalRegion
)

internal object MeasurementToRemoteSourceResolver : ReferenceResolver<Measurement, RemoteSource> by KReferenceResolver(
    Measurement::class, RemoteSource::class, Measurement::remoteSource.setter
)

internal object OperationalRestrictionToEquipmentResolver : ReferenceResolver<OperationalRestriction, Equipment> by KReferenceResolver(
    OperationalRestriction::class, Equipment::class, OperationalRestriction::addEquipment
)

internal object OrganisationRoleToOrganisationResolver : ReferenceResolver<OrganisationRole, Organisation> by KReferenceResolver(
    OrganisationRole::class, Organisation::class, OrganisationRole::organisation.setter
)

internal object PowerSystemResourceToLocationResolver : ReferenceResolver<PowerSystemResource, Location> by KReferenceResolver(
    PowerSystemResource::class, Location::class, PowerSystemResource::location.setter
)

internal object PowerTransformerToPowerTransformerEndResolver : ReferenceResolver<PowerTransformer, PowerTransformerEnd> by KReferenceResolver(
    PowerTransformer::class,
    PowerTransformerEnd::class,
    { pt, pte ->
        pte.powerTransformer = pt
        pt.addEnd(pte)
    })

internal object PowerTransformerEndToPowerTransformerResolver : ReferenceResolver<PowerTransformerEnd, PowerTransformer> by KReferenceResolver(
    PowerTransformerEnd::class, PowerTransformer::class, PowerTransformerEnd::powerTransformer.setter
)

internal object PricingStructureToTariffResolver : ReferenceResolver<PricingStructure, Tariff> by KReferenceResolver(
    PricingStructure::class, Tariff::class, PricingStructure::addTariff
)

internal object RatioTapChangerToTransformerEndResolver : ReferenceResolver<RatioTapChanger, TransformerEnd> by KReferenceResolver(
    RatioTapChanger::class, TransformerEnd::class, RatioTapChanger::transformerEnd.setter
)

internal object RemoteControlToControlResolver : ReferenceResolver<RemoteControl, Control> by KReferenceResolver(
    RemoteControl::class, Control::class, RemoteControl::control.setter
)

internal object RemoteSourceToMeasurementResolver : ReferenceResolver<RemoteSource, Measurement> by KReferenceResolver(
    RemoteSource::class, Measurement::class, RemoteSource::measurement.setter
)

internal object SubGeographicalRegionToGeographicalRegionResolver : ReferenceResolver<SubGeographicalRegion, GeographicalRegion> by KReferenceResolver(
    SubGeographicalRegion::class, GeographicalRegion::class, SubGeographicalRegion::geographicalRegion.setter
)

internal object SubGeographicalRegionToSubstationResolver : ReferenceResolver<SubGeographicalRegion, Substation> by KReferenceResolver(
    SubGeographicalRegion::class, Substation::class, SubGeographicalRegion::addSubstation
)

internal object SubstationToNormalEnergizingFeedersResolver : ReferenceResolver<Substation, Feeder> by KReferenceResolver(
    Substation::class, Feeder::class, Substation::addFeeder
)

internal object SubstationToSubGeographicalRegionResolver : ReferenceResolver<Substation, SubGeographicalRegion> by KReferenceResolver(
    Substation::class, SubGeographicalRegion::class, Substation::subGeographicalRegion.setter
)

internal object SubstationToCircuitResolver : ReferenceResolver<Substation, Circuit> by KReferenceResolver(
    Substation::class, Circuit::class, Substation::addCircuit
)

internal object SubstationToLoopResolver : ReferenceResolver<Substation, Loop> by KReferenceResolver(
    Substation::class, Loop::class, Substation::addLoop
)

internal object SubstationToEnergizedLoopResolver : ReferenceResolver<Substation, Loop> by KReferenceResolver(
    Substation::class, Loop::class, Substation::addEnergizedLoop
)

internal object TerminalToConductingEquipmentResolver : ReferenceResolver<Terminal, ConductingEquipment> by KReferenceResolver(
    Terminal::class, ConductingEquipment::class, Terminal::conductingEquipment.setter
)

internal object TerminalToConnectivityNodeResolver : ReferenceResolver<Terminal, ConnectivityNode> by KReferenceResolver(
    Terminal::class, ConnectivityNode::class, Terminal::connectivityNode.setter
)

internal object TransformerEndToTerminalResolver : ReferenceResolver<TransformerEnd, Terminal> by KReferenceResolver(
    TransformerEnd::class, Terminal::class, TransformerEnd::terminal.setter
)

internal object TransformerEndToBaseVoltageResolver : ReferenceResolver<TransformerEnd, BaseVoltage> by KReferenceResolver(
    TransformerEnd::class, BaseVoltage::class, TransformerEnd::baseVoltage.setter
)

internal object TransformerEndToRatioTapChangerResolver : ReferenceResolver<TransformerEnd, RatioTapChanger> by KReferenceResolver(
    TransformerEnd::class, RatioTapChanger::class, TransformerEnd::ratioTapChanger.setter
)

internal object UsagePointToEndDeviceResolver : ReferenceResolver<UsagePoint, EndDevice> by KReferenceResolver(
    UsagePoint::class, EndDevice::class, UsagePoint::addEndDevice
)

internal object UsagePointToEquipmentResolver : ReferenceResolver<UsagePoint, Equipment> by KReferenceResolver(
    UsagePoint::class, Equipment::class, UsagePoint::addEquipment
)

internal object UsagePointToLocationResolver : ReferenceResolver<UsagePoint, Location> by KReferenceResolver(
    UsagePoint::class, Location::class, UsagePoint::usagePointLocation.setter
)

internal object CircuitToTerminalResolver : ReferenceResolver<Circuit, Terminal> by KReferenceResolver(
    Circuit::class, Terminal::class, Circuit::addEndTerminal
)

internal object CircuitToLoopResolver : ReferenceResolver<Circuit, Loop> by KReferenceResolver(
    Circuit::class, Loop::class, Circuit::loop.setter
)

internal object CircuitToSubstationResolver : ReferenceResolver<Circuit, Substation> by KReferenceResolver(
    Circuit::class, Substation::class, Circuit::addEndSubstation
)

internal object LoopToCircuitResolver : ReferenceResolver<Loop, Circuit> by KReferenceResolver(
    Loop::class, Circuit::class, Loop::addCircuit
)

internal object LoopToSubstationResolver : ReferenceResolver<Loop, Substation> by KReferenceResolver(
    Loop::class, Substation::class, Loop::addSubstation
)

internal object LoopToEnergizingSubstationResolver : ReferenceResolver<Loop, Substation> by KReferenceResolver(
    Loop::class, Substation::class, Loop::addEnergizingSubstation
)

//-------------------------------------------//

class KReferenceResolver<T : IdentifiedObject, R : IdentifiedObject>(
    private val fromKClass: KClass<T>,
    private val toKClass: KClass<R>,
    private val resolveFun: (T, R) -> Unit
) : ReferenceResolver<T, R> {

    override val fromClass: Class<T> get() = fromKClass.java
    override val toClass: Class<R> get() = toKClass.java

    override fun resolve(from: T, to: R) {
        resolveFun(from, to)
    }
}
