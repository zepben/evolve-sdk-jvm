/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.network.translator

import com.zepben.evolve.cim.iec61968.assetinfo.*
import com.zepben.evolve.cim.iec61968.assets.AssetOrganisationRole
import com.zepben.evolve.cim.iec61968.assets.AssetOwner
import com.zepben.evolve.cim.iec61968.assets.Pole
import com.zepben.evolve.cim.iec61968.assets.Streetlight
import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.common.Organisation
import com.zepben.evolve.cim.iec61968.metering.EndDevice
import com.zepben.evolve.cim.iec61968.metering.Meter
import com.zepben.evolve.cim.iec61968.metering.UsagePoint
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.FaultIndicator
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentBranch
import com.zepben.evolve.cim.iec61970.base.meas.*
import com.zepben.evolve.cim.iec61970.base.scada.RemoteControl
import com.zepben.evolve.cim.iec61970.base.scada.RemoteSource
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.BatteryUnit
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.PhotoVoltaicUnit
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.PowerElectronicsWindUnit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Loop
import com.zepben.evolve.services.common.testdata.fillFieldsCommon
import com.zepben.evolve.services.common.translator.organisationToCim
import com.zepben.evolve.services.common.translator.organisationToGeojson
import com.zepben.evolve.services.common.translator.toPb
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.NetworkServiceComparator
import com.zepben.evolve.services.network.testdata.fillFields
import com.zepben.evolve.services.network.testdata.fillFieldsGeojson
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.fail

internal class NetworkGeojsonTranslatorTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val comparator = NetworkServiceComparator()

    // We need to create concrete types that are supported by the service so the references can be resolved below.
    private val abstractCreators = mapOf<Class<*>, (String) -> IdentifiedObject>(
        AssetOrganisationRole::class.java to { AssetOwner(it) },
        ConductingEquipment::class.java to { Junction(it) },
        EndDevice::class.java to { Meter(it) },
        Equipment::class.java to { Junction(it) },
        EquipmentContainer::class.java to { Site(it) },
        Measurement::class.java to { Discrete(it) },
        TransformerEnd::class.java to { PowerTransformerEnd(it) },
        WireInfo::class.java to { OverheadWireInfo(it) },
        Substation::class.java to { Substation(it) },
        Circuit::class.java to { Circuit(it) },
    )

    // TODO: handle Terminals. They need to be present in the service at add time as they do not use unresolved references.
    // TODO: Need specialised tests for anything with Terminals, equipment (substationId and circuitId), Locations, PowerTransformerEnds
    @Test
    internal fun `converts Geojson correctly`() {
        /************ IEC61968 ASSET INFO ************/
        validate({ CableInfo() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(cableInfoToCim(cableInfoToGeojson(it), ns)) })
        validate({ NoLoadTest() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(noLoadTestToCim(noLoadTestToGeojson(it), ns)) })
        validate({ OpenCircuitTest() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(openCircuitTestToCim(openCircuitTestToGeojson(it), ns)) })
        validate({ OverheadWireInfo() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(overheadWireInfoToCim(overheadWireInfoToGeojson(it), ns)) })
        validate({ PowerTransformerInfo() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(powerTransformerInfoToCim(powerTransformerInfoToGeojson(it), ns)) })
        validate({ ShortCircuitTest() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(shortCircuitTestToCim(shortCircuitTestToGeojson(it), ns)) })
        validate({ TransformerEndInfo() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(transformerEndInfoToCim(transformerEndInfoToGeojson(it), ns)) })
        validate({ TransformerTankInfo() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(transformerTankInfoToCim(transformerTankInfoToGeojson(it), ns)) })

        /************ IEC61968 ASSETS ************/
        validate({ AssetOwner() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(assetOwnerToCim(assetOwnerToGeojson(it), ns)) })
        validate({ Pole() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(poleToCim(poleToGeojson(it), ns)) })
        validate({ Streetlight() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(streetlightToCim(streetlightToGeojson(it), ns)) })

        /************ IEC61968 COMMON ************/
        validate({ Organisation() }, { ns, it -> it.fillFieldsCommon(ns) }, { ns, it -> ns.tryAddOrNull(organisationToCim(organisationToGeojson(it), ns)) })

        /************ IEC61968 METERING ************/
        validate({ Meter() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(meterToCim(meterToGeojson(it), ns)) })
        validate({ UsagePoint() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(usagePointToCim(usagePointToGeojson(it), ns)) })

        /************ IEC61968 OPERATIONS ************/
        validate({ OperationalRestriction() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(operationalRestrictionToCim(operationalRestrictionToGeojson(it), ns)) })

        /************ IEC61970 BASE AUXILIARY EQUIPMENT ************/
//        validate({ FaultIndicator() }, { ns, it -> it.fillFieldsGeojson(ns) }, { ns, it -> ns.tryAddOrNull(faultIndicatorToCim(faultIndicatorToGeojson(it), ns)) })

        /************ IEC61970 BASE CORE ************/
        validate({ BaseVoltage() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(baseVoltageToCim(baseVoltageToGeojson(it), ns)) })
//        validate({ Feeder() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(feederToCim(feederToGeojson(it), ns, mutableMapOf())) }) // TODO
        validate({ GeographicalRegion() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(geographicalRegionToCim(geographicalRegionToGeojson(it), ns)) })
        validate({ Site() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(siteToCim(siteToGeojson(it), ns)) })
        validate({ SubGeographicalRegion() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(subGeographicalRegionToCim(subGeographicalRegionToGeojson(it), ns)) })
        validate({ Substation() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(substationToCim(substationToGeojson(it), ns)) })

        /************ IEC61970 BASE EQUIVALENTS ************/
//        validate({ EquivalentBranch() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(equivalentBranchToCim(equivalentBranchToGeojson(it), ns)) })

        /************ IEC61970 BASE MEAS ************/
        validate({ Accumulator() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(accumulatorToCim(accumulatorToGeojson(it), ns)) })
        validate({ Analog() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(analogToCim(analogToGeojson(it), ns)) })
        validate({ Control() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(controlToCim(controlToGeojson(it), ns)) })
        validate({ Discrete() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(discreteToCim(discreteToGeojson(it), ns)) })

        /************ IEC61970 BASE SCADA ************/
        validate({ RemoteControl() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(remoteControlToCim(remoteControlToGeojson(it), ns)) })
        validate({ RemoteSource() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(remoteSourceToCim(remoteSourceToGeojson(it), ns)) })

        /************ IEC61970 BASE WIRES GENERATION PRODUCTION ************/
        validate({ BatteryUnit() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(batteryUnitToCim(batteryUnitToGeojson(it), ns)) })
        validate({ PhotoVoltaicUnit() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(photoVoltaicUnitToCim(photoVoltaicUnitToGeojson(it), ns)) })
//        validate({ PowerElectronicsConnection() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(powerElectronicsConnectionToCim(powerElectronicsConnectionToGeojson(it), ns)) })
        validate({ PowerElectronicsConnectionPhase() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(powerElectronicsConnectionPhaseToCim(powerElectronicsConnectionPhaseToGeojson(it), ns)) })
        validate({ PowerElectronicsWindUnit() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(powerElectronicsWindUnitToCim(powerElectronicsWindUnitToGeojson(it), ns)) })

        /************ IEC61970 BASE WIRES ************/
//        validate({ AcLineSegment() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(acLineSegmentToCim(acLineSegmentToGeojson(it), ns, mutableListOf())) })
//        validate({ Breaker() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(breakerToCim(breakerToGeojson(it), ns)) })
//        validate({ BusbarSection() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(busbarSectionToCim(busbarSectionToGeojson(it), ns)) })
//        validate({ Disconnector() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(disconnectorToCim(disconnectorToGeojson(it), ns)) })
//        validate({ EnergyConsumer() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(energyConsumerToCim(energyConsumerToGeojson(it), ns)) })
        validate({ EnergyConsumerPhase() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(energyConsumerPhaseToCim(energyConsumerPhaseToGeojson(it), ns)) })
//        validate({ EnergySource() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(energySourceToCim(energySourceToGeojson(it), ns)) })
        validate({ EnergySourcePhase() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(energySourcePhaseToCim(energySourcePhaseToGeojson(it), ns)) })
//        validate({ Fuse() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(fuseToCim(fuseToGeojson(it), ns)) })
//        validate({ Jumper() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(jumperToCim(jumperToGeojson(it), ns)) })
//        validate({ Junction() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(junctionToCim(junctionToGeojson(it), ns)) })
//        validate({ LinearShuntCompensator() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(linearShuntCompensatorToCim(linearShuntCompensatorToGeojson(it), ns)) })
//        validate({ LoadBreakSwitch() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(loadBreakSwitchToCim(loadBreakSwitchToGeojson(it), ns)) })
        validate({ PerLengthSequenceImpedance() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(perLengthSequenceImpedanceToCim(perLengthSequenceImpedanceToGeojson(it), ns)) })
//        validate({ PowerTransformer() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(powerTransformerToCim(powerTransformerToGeojson(it), ns)) })
        validate({ PowerTransformerEnd() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(powerTransformerEndToCim(powerTransformerEndToGeojson(it), ns)) })
        validate({ RatioTapChanger() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(ratioTapChangerToCim(ratioTapChangerToGeojson(it), ns)) })
//        validate({ Recloser() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(recloserToCim(recloserToGeojson(it), ns)) })
        validate({ TransformerStarImpedance() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(transformerStarImpedanceToCim(transformerStarImpedanceToGeojson(it), ns)) })

        /************ IEC61970 InfIEC61970 FEEDER ************/
//        validate({ Circuit() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(circuitToCim(circuitToGeojson(it), ns, mutableMapOf())) })
        validate({ Loop() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.tryAddOrNull(loopToCim(loopToGeojson(it), ns)) })
    }

    //
    // NOTE: NameType is not sent via any grpc messages at this stage, so test it separately
    //

    @Test
    internal fun createsNewNameType() {
        val pb = NameType("nt1 name").apply {
            description = "nt1 desc"
        }.toPb()

        val cim = NetworkService().addFromPb(pb)

        assertThat(cim.name, equalTo(pb.name))
        assertThat(cim.description, equalTo(pb.description))
    }

    @Test
    internal fun updatesExistingNameType() {
        val pb = NameType("nt1 name").apply {
            description = "nt1 desc"
        }.toPb()

        val nt = NameType("nt1 name")
        val cim = NetworkService().apply { addNameType(nt) }.addFromPb(pb)

        assertThat(cim, sameInstance(nt))
        assertThat(cim.description, equalTo(pb.description))
    }

    private inline fun <reified T : IdentifiedObject> validate(creator: () -> T, filler: (NetworkService, T) -> Unit, adder: (NetworkService, T) -> T?) {
        val cim = creator()
        val blankDifferences = comparator.compare(cim, adder(NetworkService(), cim)!!).differences
        assertThat("Failed to convert blank ${T::class.simpleName}:${blankDifferences}", blankDifferences, anEmptyMap())

        val ns = NetworkService()
        filler(ns, cim)
        removeUnsentReferences(cim)

        val populatedDifferences = comparator.compare(cim, addWithUnresolvedReferences(cim, adder)).differences
        assertThat("Failed to convert populated ${T::class.simpleName}:${populatedDifferences}", populatedDifferences, anEmptyMap())
    }

    private inline fun <reified T : IdentifiedObject> removeUnsentReferences(cim: T) {
        if (cim is EquipmentContainer)
            cim.clearEquipment()

        if (cim is OperationalRestriction)
            cim.clearEquipment()

        if (cim is Feeder)
            cim.clearCurrentEquipment()

        if (cim is ConnectivityNode)
            cim.clearTerminals()
    }

    private inline fun <reified T : IdentifiedObject> addWithUnresolvedReferences(cim: T, adder: (NetworkService, T) -> T?): T {
        // We need to convert the populated item before we check the differences so we can complete the unresolved references.
        val service = NetworkService()
        val convertedCim = adder(service, cim)!!
        service.unresolvedReferences().toList().forEach { ref ->
            try {
                val io = abstractCreators[ref.resolver.toClass]?.invoke(ref.toMrid) ?: ref.resolver.toClass.getDeclaredConstructor(String::class.java)
                    .newInstance(ref.toMrid)
                io.also { service.tryAdd(it) }
            } catch (e: Exception) {
                // If this fails you need to add a concrete type mapping to the abstractCreators map at the top of this class.
                fail("Failed to create unresolved reference for ${ref.resolver.toClass}.", e)
            }
        }
        return convertedCim
    }

}
