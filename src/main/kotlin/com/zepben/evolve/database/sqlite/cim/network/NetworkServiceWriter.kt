/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.network

import com.zepben.evolve.cim.iec61968.assetinfo.*
import com.zepben.evolve.cim.iec61968.assets.AssetOwner
import com.zepben.evolve.cim.iec61968.assets.Pole
import com.zepben.evolve.cim.iec61968.assets.Streetlight
import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.common.Organisation
import com.zepben.evolve.cim.iec61968.infiec61968.infassetinfo.CurrentTransformerInfo
import com.zepben.evolve.cim.iec61968.infiec61968.infassetinfo.PotentialTransformerInfo
import com.zepben.evolve.cim.iec61968.infiec61968.infassetinfo.RelayInfo
import com.zepben.evolve.cim.iec61968.metering.Meter
import com.zepben.evolve.cim.iec61968.metering.UsagePoint
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.CurrentTransformer
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.FaultIndicator
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.PotentialTransformer
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentBranch
import com.zepben.evolve.cim.iec61970.base.meas.Accumulator
import com.zepben.evolve.cim.iec61970.base.meas.Analog
import com.zepben.evolve.cim.iec61970.base.meas.Control
import com.zepben.evolve.cim.iec61970.base.meas.Discrete
import com.zepben.evolve.cim.iec61970.base.protection.*
import com.zepben.evolve.cim.iec61970.base.scada.RemoteControl
import com.zepben.evolve.cim.iec61970.base.scada.RemoteSource
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.BatteryUnit
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.PhotoVoltaicUnit
import com.zepben.evolve.cim.iec61970.base.wires.generation.production.PowerElectronicsWindUnit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Loop
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.LvFeeder
import com.zepben.evolve.cim.iec61970.infiec61970.wires.generation.production.EvChargingUnit
import com.zepben.evolve.database.sqlite.common.BaseServiceWriter
import com.zepben.evolve.services.network.NetworkService

/**
 * A class for writing a [NetworkService] into the database.
 *
 * @param service The [NetworkService] to save to the database.
 * @param databaseTables The [NetworkDatabaseTables] to add to the database.
 */
class NetworkServiceWriter @JvmOverloads constructor(
    override val service: NetworkService,
    databaseTables: NetworkDatabaseTables,
    override val writer: NetworkCimWriter = NetworkCimWriter(databaseTables)
) : BaseServiceWriter(service, writer) {

    override fun doSave(): Boolean =
        saveEach<CableInfo>(writer::save)
            .andSaveEach<OverheadWireInfo>(writer::save)
            .andSaveEach<PowerTransformerInfo>(writer::save)
            .andSaveEach<TransformerTankInfo>(writer::save)
            .andSaveEach<NoLoadTest>(writer::save)
            .andSaveEach<OpenCircuitTest>(writer::save)
            .andSaveEach<ShortCircuitTest>(writer::save)
            .andSaveEach<ShuntCompensatorInfo>(writer::save)
            .andSaveEach<SwitchInfo>(writer::save)
            .andSaveEach<TransformerEndInfo>(writer::save)
            .andSaveEach<AssetOwner>(writer::save)
            .andSaveEach<Pole>(writer::save)
            .andSaveEach<Streetlight>(writer::save)
            .andSaveEach<Location>(writer::save)
            .andSaveEach<Organisation>(writer::save)
            .andSaveEach<Meter>(writer::save)
            .andSaveEach<UsagePoint>(writer::save)
            .andSaveEach<OperationalRestriction>(writer::save)
            .andSaveEach<FaultIndicator>(writer::save)
            .andSaveEach<BaseVoltage>(writer::save)
            .andSaveEach<ConnectivityNode>(writer::save)
            .andSaveEach<Feeder>(writer::save)
            .andSaveEach<GeographicalRegion>(writer::save)
            .andSaveEach<Site>(writer::save)
            .andSaveEach<SubGeographicalRegion>(writer::save)
            .andSaveEach<Substation>(writer::save)
            .andSaveEach<Terminal>(writer::save)
            .andSaveEach<EquivalentBranch>(writer::save)
            .andSaveEach<BatteryUnit>(writer::save)
            .andSaveEach<PhotoVoltaicUnit>(writer::save)
            .andSaveEach<PowerElectronicsWindUnit>(writer::save)
            .andSaveEach<AcLineSegment>(writer::save)
            .andSaveEach<Breaker>(writer::save)
            .andSaveEach<LoadBreakSwitch>(writer::save)
            .andSaveEach<BusbarSection>(writer::save)
            .andSaveEach<Disconnector>(writer::save)
            .andSaveEach<EnergyConsumer>(writer::save)
            .andSaveEach<EnergyConsumerPhase>(writer::save)
            .andSaveEach<EnergySource>(writer::save)
            .andSaveEach<EnergySourcePhase>(writer::save)
            .andSaveEach<Fuse>(writer::save)
            .andSaveEach<Jumper>(writer::save)
            .andSaveEach<Junction>(writer::save)
            .andSaveEach<LinearShuntCompensator>(writer::save)
            .andSaveEach<PerLengthSequenceImpedance>(writer::save)
            .andSaveEach<PowerElectronicsConnection>(writer::save)
            .andSaveEach<PowerElectronicsConnectionPhase>(writer::save)
            .andSaveEach<PowerTransformer>(writer::save)
            .andSaveEach<PowerTransformerEnd>(writer::save)
            .andSaveEach<RatioTapChanger>(writer::save)
            .andSaveEach<Recloser>(writer::save)
            .andSaveEach<TransformerStarImpedance>(writer::save)
            .andSaveEach<Circuit>(writer::save)
            .andSaveEach<Loop>(writer::save)
            .andSaveEach<LvFeeder>(writer::save)
            .andSaveEach<Analog>(writer::save)
            .andSaveEach<Accumulator>(writer::save)
            .andSaveEach<Discrete>(writer::save)
            .andSaveEach<Control>(writer::save)
            .andSaveEach<RemoteControl>(writer::save)
            .andSaveEach<RemoteSource>(writer::save)
            .andSaveEach<CurrentTransformerInfo>(writer::save)
            .andSaveEach<PotentialTransformerInfo>(writer::save)
            .andSaveEach<CurrentTransformer>(writer::save)
            .andSaveEach<PotentialTransformer>(writer::save)
            .andSaveEach<RelayInfo>(writer::save)
            .andSaveEach<CurrentRelay>(writer::save)
            .andSaveEach<TapChangerControl>(writer::save)
            .andSaveEach<EvChargingUnit>(writer::save)
            .andSaveEach<DistanceRelay>(writer::save)
            .andSaveEach<ProtectionRelayScheme>(writer::save)
            .andSaveEach<ProtectionRelaySystem>(writer::save)
            .andSaveEach<VoltageRelay>(writer::save)
            .andSaveEach<Ground>(writer::save)
            .andSaveEach<GroundDisconnector>(writer::save)
            .andSaveEach<SeriesCompensator>(writer::save)

}
