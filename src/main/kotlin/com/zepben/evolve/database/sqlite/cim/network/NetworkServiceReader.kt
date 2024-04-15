/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.cim.network

import com.zepben.evolve.database.sqlite.cim.CimReader
import com.zepben.evolve.database.sqlite.common.BaseServiceReader
import com.zepben.evolve.database.sqlite.cim.tables.associations.*
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assetinfo.*
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TableAssetOwners
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TablePoles
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.assets.TableStreetlights
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableLocationStreetAddresses
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableLocations
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TableOrganisations
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.common.TablePositionPoints
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.infiec61968.infassetinfo.TableCurrentTransformerInfo
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.infiec61968.infassetinfo.TablePotentialTransformerInfo
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.infiec61968.infassetinfo.TableRecloseDelays
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.infiec61968.infassetinfo.TableRelayInfo
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.metering.TableMeters
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.metering.TableUsagePoints
import com.zepben.evolve.database.sqlite.cim.tables.iec61968.operations.TableOperationalRestrictions
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.auxiliaryequipment.TableCurrentTransformers
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.auxiliaryequipment.TableFaultIndicators
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.auxiliaryequipment.TablePotentialTransformers
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.core.*
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.equivalents.TableEquivalentBranches
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.meas.TableAccumulators
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.meas.TableAnalogs
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.meas.TableControls
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.meas.TableDiscretes
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.protection.*
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.scada.TableRemoteControls
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.scada.TableRemoteSources
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.*
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.generation.production.TableBatteryUnits
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.generation.production.TablePhotoVoltaicUnits
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.generation.production.TablePowerElectronicsWindUnits
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.infiec61970.feeder.TableCircuits
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.infiec61970.feeder.TableLoops
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.infiec61970.feeder.TableLvFeeders
import com.zepben.evolve.database.sqlite.cim.tables.iec61970.infiec61970.wires.generation.production.TableEvChargingUnits
import com.zepben.evolve.services.network.NetworkService
import java.sql.Connection

/**
 * A class for reading a [NetworkService] from the database.
 *
 * @param service The [NetworkService] to populate from the database.
 * @param databaseTables The tables available in the database.
 * @param connection A connection to the database.
 *
 * @property reader The [CimReader] used to load the objects from the database.
 */
class NetworkServiceReader @JvmOverloads constructor(
    service: NetworkService,
    databaseTables: NetworkDatabaseTables,
    connection: Connection,
    override val reader: NetworkCimReader = NetworkCimReader(service)
) : BaseServiceReader(databaseTables, connection, reader) {

    override fun doLoad(): Boolean =
        loadEach<TableCableInfo>(reader::load)
            .andLoadEach<TableOverheadWireInfo>(reader::load)
            .andLoadEach<TablePowerTransformerInfo>(reader::load)
            .andLoadEach<TableTransformerTankInfo>(reader::load)
            .andLoadEach<TableNoLoadTests>(reader::load)
            .andLoadEach<TableOpenCircuitTests>(reader::load)
            .andLoadEach<TableShortCircuitTests>(reader::load)
            .andLoadEach<TableShuntCompensatorInfo>(reader::load)
            .andLoadEach<TableSwitchInfo>(reader::load)
            .andLoadEach<TableTransformerEndInfo>(reader::load)
            .andLoadEach<TableCurrentTransformerInfo>(reader::load)
            .andLoadEach<TablePotentialTransformerInfo>(reader::load)
            .andLoadEach<TableRelayInfo>(reader::load)
            .andLoadEach<TableRecloseDelays>(reader::load)
            .andLoadEach<TableLocations>(reader::load)
            .andLoadEach<TableOrganisations>(reader::load)
            .andLoadEach<TableAssetOwners>(reader::load)
            .andLoadEach<TablePoles>(reader::load)
            .andLoadEach<TableStreetlights>(reader::load)
            .andLoadEach<TableMeters>(reader::load)
            .andLoadEach<TableUsagePoints>(reader::load)
            .andLoadEach<TableOperationalRestrictions>(reader::load)
            .andLoadEach<TableBaseVoltages>(reader::load)
            .andLoadEach<TableConnectivityNodes>(reader::load)
            .andLoadEach<TableGeographicalRegions>(reader::load)
            .andLoadEach<TableSubGeographicalRegions>(reader::load)
            .andLoadEach<TableSubstations>(reader::load)
            .andLoadEach<TableSites>(reader::load)
            .andLoadEach<com.zepben.evolve.database.sqlite.cim.tables.iec61970.base.wires.TablePerLengthSequenceImpedances>(reader::load)
            .andLoadEach<TableEquivalentBranches>(reader::load)
            .andLoadEach<TableAcLineSegments>(reader::load)
            .andLoadEach<TableBreakers>(reader::load)
            .andLoadEach<TableLoadBreakSwitches>(reader::load)
            .andLoadEach<TableBusbarSections>(reader::load)
            .andLoadEach<TableCurrentRelays>(reader::load)
            .andLoadEach<TableDistanceRelays>(reader::load)
            .andLoadEach<TableVoltageRelays>(reader::load)
            .andLoadEach<TableProtectionRelayFunctionThresholds>(reader::load)
            .andLoadEach<TableProtectionRelayFunctionTimeLimits>(reader::load)
            .andLoadEach<TableProtectionRelaySystems>(reader::load)
            .andLoadEach<TableProtectionRelaySchemes>(reader::load)
            .andLoadEach<TableDisconnectors>(reader::load)
            .andLoadEach<TableEnergyConsumers>(reader::load)
            .andLoadEach<TableEnergyConsumerPhases>(reader::load)
            .andLoadEach<TableEnergySources>(reader::load)
            .andLoadEach<TableEnergySourcePhases>(reader::load)
            .andLoadEach<TableFuses>(reader::load)
            .andLoadEach<TableJumpers>(reader::load)
            .andLoadEach<TableJunctions>(reader::load)
            .andLoadEach<TableGrounds>(reader::load)
            .andLoadEach<TableGroundDisconnectors>(reader::load)
            .andLoadEach<TableSeriesCompensators>(reader::load)
            .andLoadEach<TableLinearShuntCompensators>(reader::load)
            .andLoadEach<TablePowerTransformers>(reader::load)
            .andLoadEach<TableReclosers>(reader::load)
            .andLoadEach<TablePowerElectronicsConnections>(reader::load)
            .andLoadEach<TableTerminals>(reader::load)
            .andLoadEach<TableTapChangerControls>(reader::load)
            .andLoadEach<TablePowerElectronicsConnectionPhases>(reader::load)
            .andLoadEach<TableBatteryUnits>(reader::load)
            .andLoadEach<TablePhotoVoltaicUnits>(reader::load)
            .andLoadEach<TablePowerElectronicsWindUnits>(reader::load)
            .andLoadEach<TableEvChargingUnits>(reader::load)
            .andLoadEach<TableTransformerStarImpedances>(reader::load)
            .andLoadEach<TablePowerTransformerEnds>(reader::load)
            .andLoadEach<TablePowerTransformerEndRatings>(reader::load)
            .andLoadEach<TableRatioTapChangers>(reader::load)
            .andLoadEach<TableCurrentTransformers>(reader::load)
            .andLoadEach<TableFaultIndicators>(reader::load)
            .andLoadEach<TablePotentialTransformers>(reader::load)
            .andLoadEach<TableFeeders>(reader::load)
            .andLoadEach<TableLoops>(reader::load)
            .andLoadEach<TableLvFeeders>(reader::load)
            .andLoadEach<TableCircuits>(reader::load)
            .andLoadEach<TablePositionPoints>(reader::load)
            .andLoadEach<TableLocationStreetAddresses>(reader::load)
            .andLoadEach<TableAssetOrganisationRolesAssets>(reader::load)
            .andLoadEach<TableUsagePointsEndDevices>(reader::load)
            .andLoadEach<TableEquipmentUsagePoints>(reader::load)
            .andLoadEach<TableEquipmentOperationalRestrictions>(reader::load)
            .andLoadEach<TableEquipmentEquipmentContainers>(reader::load)
            .andLoadEach<TableCircuitsSubstations>(reader::load)
            .andLoadEach<TableCircuitsTerminals>(reader::load)
            .andLoadEach<TableLoopsSubstations>(reader::load)
            .andLoadEach<TableProtectionRelayFunctionsProtectedSwitches>(reader::load)
            .andLoadEach<com.zepben.evolve.database.sqlite.cim.tables.associations.TableProtectionRelayFunctionsSensors>(reader::load)
            .andLoadEach<TableProtectionRelaySchemesProtectionRelayFunctions>(reader::load)
            .andLoadEach<TableControls>(reader::load)
            .andLoadEach<TableRemoteControls>(reader::load)
            .andLoadEach<TableRemoteSources>(reader::load)
            .andLoadEach<TableAnalogs>(reader::load)
            .andLoadEach<TableAccumulators>(reader::load)
            .andLoadEach<TableDiscretes>(reader::load)

}
