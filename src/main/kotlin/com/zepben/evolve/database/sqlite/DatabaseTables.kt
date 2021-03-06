/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite

import com.zepben.evolve.database.sqlite.tables.MissingTableConfigException
import com.zepben.evolve.database.sqlite.tables.SqliteTable
import com.zepben.evolve.database.sqlite.tables.TableMetadataDataSources
import com.zepben.evolve.database.sqlite.tables.TableVersion
import com.zepben.evolve.database.sqlite.tables.associations.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assetinfo.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.TableAssetOwners
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.TablePoles
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.TableStreetlights
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableLocationStreetAddresses
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableLocations
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TableOrganisations
import com.zepben.evolve.database.sqlite.tables.iec61968.common.TablePositionPoints
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TableCustomerAgreements
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TableCustomers
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TablePricingStructures
import com.zepben.evolve.database.sqlite.tables.iec61968.customers.TableTariffs
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableMeters
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableUsagePoints
import com.zepben.evolve.database.sqlite.tables.iec61968.operations.TableOperationalRestrictions
import com.zepben.evolve.database.sqlite.tables.iec61970.base.auxiliaryequipment.TableFaultIndicators
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.diagramlayout.TableDiagramObjectPoints
import com.zepben.evolve.database.sqlite.tables.iec61970.base.diagramlayout.TableDiagramObjects
import com.zepben.evolve.database.sqlite.tables.iec61970.base.diagramlayout.TableDiagrams
import com.zepben.evolve.database.sqlite.tables.iec61970.base.equivalents.TableEquivalentBranches
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableAccumulators
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableAnalogs
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableControls
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.TableDiscretes
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteControls
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteSources
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TableBatteryUnit
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TablePhotoVoltaicUnit
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TablePowerElectronicsWindUnit
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableCircuits
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableLoops
import java.sql.Connection
import java.sql.PreparedStatement

class DatabaseTables {

    /**
     * Note this is no longer populated by reflection because the reflection was slow
     * and could make the tests take a long time to run.
     */
    private val tables: Map<Class<out SqliteTable>, SqliteTable> = createTables()
    private val insertStatements = mutableMapOf<Class<out SqliteTable>, PreparedStatement>()

    @Throws(MissingTableConfigException::class)
    fun <T : SqliteTable> getTable(clazz: Class<T>): T {
        val table = tables[clazz]
            ?: throw MissingTableConfigException("INTERNAL ERROR: No table has been registered for " + clazz.simpleName + ". You might want to consider fixing that.")

        return clazz.cast(table)
    }

    @Throws(MissingTableConfigException::class)
    fun getInsert(clazz: Class<out SqliteTable>): PreparedStatement {
        return insertStatements[clazz]
            ?: throw MissingTableConfigException("INTERNAL ERROR: No prepared statement has been registered for " + clazz.simpleName + ". You might want to consider fixing that.")
    }

    fun forEachTable(action: (SqliteTable) -> Unit) {
        tables.values.forEach(action)
    }

    fun prepareInsertStatements(connection: Connection, getPreparedStatement: (Connection, String) -> PreparedStatement) {
        insertStatements.clear()
        for ((key, value) in tables) insertStatements[key] = getPreparedStatement(connection, value.preparedInsertSql())
    }

    private fun createTables(): Map<Class<out SqliteTable>, SqliteTable> = listOf(
        TableAcLineSegments(),
        TableAccumulators(),
        TableAnalogs(),
        TableAssetOrganisationRolesAssets(),
        TableAssetOwners(),
        TableBaseVoltages(),
        TableBatteryUnit(),
        TableBreakers(),
        TableBusbarSections(),
        TableCableInfo(),
        TableCircuits(),
        TableCircuitsSubstations(),
        TableCircuitsTerminals(),
        TableConnectivityNodes(),
        TableControls(),
        TableCustomerAgreements(),
        TableCustomerAgreementsPricingStructures(),
        TableCustomers(),
        TableDiagramObjectPoints(),
        TableDiagramObjects(),
        TableDiagrams(),
        TableDisconnectors(),
        TableDiscretes(),
        TableEnergyConsumerPhases(),
        TableEnergyConsumers(),
        TableEnergySourcePhases(),
        TableEnergySources(),
        TableEquipmentEquipmentContainers(),
        TableEquipmentOperationalRestrictions(),
        TableEquipmentUsagePoints(),
        TableEquivalentBranches(),
        TableFaultIndicators(),
        TableFeeders(),
        TableFuses(),
        TableGeographicalRegions(),
        TableJumpers(),
        TableJunctions(),
        TableLinearShuntCompensators(),
        TableLoadBreakSwitches(),
        TableLocationStreetAddresses(),
        TableLocations(),
        TableLoops(),
        TableLoopsSubstations(),
        TableMetadataDataSources(),
        TableMeters(),
        TableNameTypes(),
        TableNames(),
        TableNoLoadTests(),
        TableOpenCircuitTests(),
        TableOperationalRestrictions(),
        TableOrganisations(),
        TableOverheadWireInfo(),
        TablePerLengthSequenceImpedances(),
        TablePhotoVoltaicUnit(),
        TablePoles(),
        TablePositionPoints(),
        TablePowerElectronicsConnection(),
        TablePowerElectronicsConnectionPhases(),
        TablePowerElectronicsWindUnit(),
        TablePowerTransformerEnds(),
        TablePowerTransformerInfo(),
        TablePowerTransformers(),
        TablePricingStructures(),
        TablePricingStructuresTariffs(),
        TableRatioTapChangers(),
        TableReclosers(),
        TableRemoteControls(),
        TableRemoteSources(),
        TableShortCircuitTests(),
        TableSites(),
        TableStreetlights(),
        TableSubGeographicalRegions(),
        TableSubstations(),
        TableTariffs(),
        TableTerminals(),
        TableTransformerEndInfo(),
        TableTransformerStarImpedance(),
        TableTransformerTankInfo(),
        TableUsagePoints(),
        TableUsagePointsEndDevices(),
        TableVersion(),
    ).associateBy { it::class.java }
}
