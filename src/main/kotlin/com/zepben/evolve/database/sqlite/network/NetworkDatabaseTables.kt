/*
 * Copyright 2024 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.network

import com.zepben.evolve.database.sqlite.common.cim.CimDatabaseTables
import com.zepben.evolve.database.sqlite.tables.SqliteTable
import com.zepben.evolve.database.sqlite.tables.associations.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assetinfo.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.TableAssetOwners
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.TablePoles
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.TableStreetlights
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableLocationStreetAddresses
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableLocations
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableOrganisations
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TablePositionPoints
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TableCurrentTransformerInfo
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TablePotentialTransformerInfo
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TableRecloseDelays
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TableRelayInfo
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableMeters
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableUsagePoints
import com.zepben.evolve.database.sqlite.tables.iec61968.operations.TableOperationalRestrictions
import com.zepben.evolve.database.sqlite.tables.iec61970.base.auxiliaryequipment.TableCurrentTransformers
import com.zepben.evolve.database.sqlite.tables.iec61970.base.auxiliaryequipment.TableFaultIndicators
import com.zepben.evolve.database.sqlite.tables.iec61970.base.auxiliaryequipment.TablePotentialTransformers
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.equivalents.TableEquivalentBranches
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableAccumulators
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableAnalogs
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableControls
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableDiscretes
import com.zepben.evolve.database.sqlite.tables.iec61970.base.protection.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteControls
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteSources
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TableBatteryUnits
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TablePhotoVoltaicUnits
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TablePowerElectronicsWindUnits
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableCircuits
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableLoops
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableLvFeeders
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.wires.generation.production.TableEvChargingUnits

/**
 * The collection of tables for our network databases.
 */
class NetworkDatabaseTables : CimDatabaseTables() {

    override val includedTables: Sequence<SqliteTable> =
        super.includedTables + sequenceOf(
            TableAcLineSegments(),
            TableAccumulators(),
            TableAnalogs(),
            TableAssetOrganisationRolesAssets(),
            TableAssetOwners(),
            TableBaseVoltages(),
            TableBatteryUnits(),
            TableBreakers(),
            TableBusbarSections(),
            TableCableInfo(),
            TableCircuits(),
            TableCircuitsSubstations(),
            TableCircuitsTerminals(),
            TableConnectivityNodes(),
            TableControls(),
            TableCurrentRelays(),
            TableCurrentTransformerInfo(),
            TableCurrentTransformers(),
            TableDisconnectors(),
            TableDiscretes(),
            TableDistanceRelays(),
            TableEnergyConsumerPhases(),
            TableEnergyConsumers(),
            TableEnergySourcePhases(),
            TableEnergySources(),
            TableEquipmentEquipmentContainers(),
            TableEquipmentOperationalRestrictions(),
            TableEquipmentUsagePoints(),
            TableEquivalentBranches(),
            TableEvChargingUnits(),
            TableFaultIndicators(),
            TableFeeders(),
            TableFuses(),
            TableGeographicalRegions(),
            TableGrounds(),
            TableGroundDisconnectors(),
            TableJumpers(),
            TableJunctions(),
            TableLinearShuntCompensators(),
            TableLoadBreakSwitches(),
            TableLocationStreetAddresses(),
            TableLocations(),
            TableLoops(),
            TableLoopsSubstations(),
            TableLvFeeders(),
            TableMeters(),
            TableNoLoadTests(),
            TableOpenCircuitTests(),
            TableOperationalRestrictions(),
            TableOrganisations(),
            TableOverheadWireInfo(),
            TablePerLengthSequenceImpedances(),
            TablePhotoVoltaicUnits(),
            TablePoles(),
            TablePositionPoints(),
            TablePotentialTransformerInfo(),
            TablePotentialTransformers(),
            TablePowerElectronicsConnections(),
            TablePowerElectronicsConnectionPhases(),
            TablePowerElectronicsWindUnits(),
            TablePowerTransformerEnds(),
            TablePowerTransformerEndRatings(),
            TablePowerTransformerInfo(),
            TablePowerTransformers(),
            TableProtectionRelayFunctionThresholds(),
            TableProtectionRelayFunctionTimeLimits(),
            TableProtectionRelayFunctionsProtectedSwitches(),
            TableProtectionRelayFunctionsSensors(),
            TableProtectionRelaySchemes(),
            TableProtectionRelaySchemesProtectionRelayFunctions(),
            TableProtectionRelaySystems(),
            TableRatioTapChangers(),
            TableReclosers(),
            TableRecloseDelays(),
            TableRelayInfo(),
            TableRemoteControls(),
            TableRemoteSources(),
            TableSeriesCompensators(),
            TableShortCircuitTests(),
            TableShuntCompensatorInfo(),
            TableSites(),
            TableStreetlights(),
            TableSubGeographicalRegions(),
            TableSubstations(),
            TableSwitchInfo(),
            TableTapChangerControls(),
            TableTerminals(),
            TableTransformerEndInfo(),
            TableTransformerStarImpedances(),
            TableTransformerTankInfo(),
            TableUsagePoints(),
            TableUsagePointsEndDevices(),
            TableVoltageRelays()
        )

}
