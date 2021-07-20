/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.database.sqlite.writers

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
import com.zepben.evolve.database.sqlite.DatabaseTables
import com.zepben.evolve.database.sqlite.extensions.setNullableDouble
import com.zepben.evolve.database.sqlite.extensions.setNullableInt
import com.zepben.evolve.database.sqlite.extensions.setNullableLong
import com.zepben.evolve.database.sqlite.extensions.setNullableString
import com.zepben.evolve.database.sqlite.tables.associations.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assetinfo.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.*
import com.zepben.evolve.database.sqlite.tables.iec61968.common.*
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableEndDevices
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableMeters
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableUsagePoints
import com.zepben.evolve.database.sqlite.tables.iec61968.operations.TableOperationalRestrictions
import com.zepben.evolve.database.sqlite.tables.iec61970.base.auxiliaryequipment.TableAuxiliaryEquipment
import com.zepben.evolve.database.sqlite.tables.iec61970.base.auxiliaryequipment.TableFaultIndicators
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.equivalents.TableEquivalentBranches
import com.zepben.evolve.database.sqlite.tables.iec61970.base.equivalents.TableEquivalentEquipment
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteControls
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemotePoints
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteSources
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TableBatteryUnit
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TablePhotoVoltaicUnit
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TablePowerElectronicsUnit
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.TablePowerElectronicsWindUnit
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableCircuits
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableLoops
import java.sql.PreparedStatement


@Suppress("SameParameterValue")
class NetworkCIMWriter(databaseTables: DatabaseTables) : BaseCIMWriter(databaseTables) {

    /************ IEC61968 ASSET INFO ************/

    fun save(cableInfo: CableInfo): Boolean {
        val table = databaseTables.getTable(TableCableInfo::class.java)
        val insert = databaseTables.getInsert(TableCableInfo::class.java)

        return saveWireInfo(table, insert, cableInfo, "cable info")
    }

    fun save(noLoadTest: NoLoadTest): Boolean {
        val table = databaseTables.getTable(TableNoLoadTests::class.java)
        val insert = databaseTables.getInsert(TableNoLoadTests::class.java)

        insert.setNullableInt(table.ENERGISED_END_VOLTAGE.queryIndex, noLoadTest.energisedEndVoltage)
        insert.setNullableDouble(table.EXCITING_CURRENT.queryIndex, noLoadTest.excitingCurrent)
        insert.setNullableDouble(table.EXCITING_CURRENT_ZERO.queryIndex, noLoadTest.excitingCurrentZero)
        insert.setNullableInt(table.LOSS.queryIndex, noLoadTest.loss)
        insert.setNullableInt(table.LOSS_ZERO.queryIndex, noLoadTest.lossZero)

        return saveTransformerTest(table, insert, noLoadTest, "no load test")
    }

    fun save(openCircuitTest: OpenCircuitTest): Boolean {
        val table = databaseTables.getTable(TableOpenCircuitTests::class.java)
        val insert = databaseTables.getInsert(TableOpenCircuitTests::class.java)

        insert.setNullableInt(table.ENERGISED_END_STEP.queryIndex, openCircuitTest.energisedEndStep)
        insert.setNullableInt(table.ENERGISED_END_VOLTAGE.queryIndex, openCircuitTest.energisedEndVoltage)
        insert.setNullableInt(table.OPEN_END_STEP.queryIndex, openCircuitTest.openEndStep)
        insert.setNullableInt(table.OPEN_END_VOLTAGE.queryIndex, openCircuitTest.openEndVoltage)
        insert.setNullableDouble(table.PHASE_SHIFT.queryIndex, openCircuitTest.phaseShift)

        return saveTransformerTest(table, insert, openCircuitTest, "open circuit test")
    }

    fun save(overheadWireInfo: OverheadWireInfo): Boolean {
        val table = databaseTables.getTable(TableOverheadWireInfo::class.java)
        val insert = databaseTables.getInsert(TableOverheadWireInfo::class.java)

        return saveWireInfo(table, insert, overheadWireInfo, "overhead wire info")
    }

    fun save(powerTransformerInfo: PowerTransformerInfo): Boolean {
        val table = databaseTables.getTable(TablePowerTransformerInfo::class.java)
        val insert = databaseTables.getInsert(TablePowerTransformerInfo::class.java)

        return saveAssetInfo(table, insert, powerTransformerInfo, "power transformer info")
    }

    fun save(shortCircuitTest: ShortCircuitTest): Boolean {
        val table = databaseTables.getTable(TableShortCircuitTests::class.java)
        val insert = databaseTables.getInsert(TableShortCircuitTests::class.java)

        insert.setNullableDouble(table.CURRENT.queryIndex, shortCircuitTest.current)
        insert.setNullableInt(table.ENERGISED_END_STEP.queryIndex, shortCircuitTest.energisedEndStep)
        insert.setNullableInt(table.GROUNDED_END_STEP.queryIndex, shortCircuitTest.groundedEndStep)
        insert.setNullableDouble(table.LEAKAGE_IMPEDANCE.queryIndex, shortCircuitTest.leakageImpedance)
        insert.setNullableDouble(table.LEAKAGE_IMPEDANCE_ZERO.queryIndex, shortCircuitTest.leakageImpedanceZero)
        insert.setNullableInt(table.LOSS.queryIndex, shortCircuitTest.loss)
        insert.setNullableInt(table.LOSS_ZERO.queryIndex, shortCircuitTest.lossZero)
        insert.setNullableInt(table.POWER.queryIndex, shortCircuitTest.power)
        insert.setNullableDouble(table.VOLTAGE.queryIndex, shortCircuitTest.voltage)
        insert.setNullableDouble(table.VOLTAGE_OHMIC_PART.queryIndex, shortCircuitTest.voltageOhmicPart)

        return saveTransformerTest(table, insert, shortCircuitTest, "short circuit test")
    }

    fun save(transformerEndInfo: TransformerEndInfo): Boolean {
        val table = databaseTables.getTable(TableTransformerEndInfo::class.java)
        val insert = databaseTables.getInsert(TableTransformerEndInfo::class.java)

        insert.setString(table.CONNECTION_KIND.queryIndex, transformerEndInfo.connectionKind.name)
        insert.setNullableInt(table.EMERGENCY_S.queryIndex, transformerEndInfo.emergencyS)
        insert.setInt(table.END_NUMBER.queryIndex, transformerEndInfo.endNumber)
        insert.setNullableInt(table.INSULATION_U.queryIndex, transformerEndInfo.insulationU)
        insert.setNullableInt(table.PHASE_ANGLE_CLOCK.queryIndex, transformerEndInfo.phaseAngleClock)
        insert.setNullableDouble(table.R.queryIndex, transformerEndInfo.r)
        insert.setNullableInt(table.RATED_S.queryIndex, transformerEndInfo.ratedS)
        insert.setNullableInt(table.RATED_U.queryIndex, transformerEndInfo.ratedU)
        insert.setNullableInt(table.SHORT_TERM_S.queryIndex, transformerEndInfo.shortTermS)
        insert.setNullableString(table.TRANSFORMER_TANK_INFO_MRID.queryIndex, transformerEndInfo.transformerTankInfo?.mRID)
        insert.setNullableString(table.ENERGISED_END_NO_LOAD_TESTS.queryIndex, transformerEndInfo.energisedEndNoLoadTests?.mRID)
        insert.setNullableString(table.ENERGISED_END_SHORT_CIRCUIT_TESTS.queryIndex, transformerEndInfo.energisedEndShortCircuitTests?.mRID)
        insert.setNullableString(table.GROUNDED_END_SHORT_CIRCUIT_TESTS.queryIndex, transformerEndInfo.groundedEndShortCircuitTests?.mRID)
        insert.setNullableString(table.OPEN_END_OPEN_CIRCUIT_TESTS.queryIndex, transformerEndInfo.openEndOpenCircuitTests?.mRID)
        insert.setNullableString(table.ENERGISED_END_OPEN_CIRCUIT_TESTS.queryIndex, transformerEndInfo.energisedEndOpenCircuitTests?.mRID)

        return saveAssetInfo(table, insert, transformerEndInfo, "transformer end info")
    }

    fun save(transformerTankInfo: TransformerTankInfo): Boolean {
        val table = databaseTables.getTable(TableTransformerTankInfo::class.java)
        val insert = databaseTables.getInsert(TableTransformerTankInfo::class.java)

        insert.setNullableString(table.POWER_TRANSFORMER_INFO_MRID.queryIndex, transformerTankInfo.powerTransformerInfo?.mRID)

        return saveAssetInfo(table, insert, transformerTankInfo, "transformer tank info")
    }

    private fun saveTransformerTest(table: TableTransformerTest, insert: PreparedStatement, transformerTest: TransformerTest, description: String): Boolean {
        insert.setNullableInt(table.BASE_POWER.queryIndex, transformerTest.basePower)
        insert.setNullableDouble(table.TEMPERATURE.queryIndex, transformerTest.temperature)

        return saveIdentifiedObject(table, insert, transformerTest, description)
    }

    private fun saveWireInfo(table: TableWireInfo, insert: PreparedStatement, wireInfo: WireInfo, description: String): Boolean {
        insert.setNullableInt(table.RATED_CURRENT.queryIndex, wireInfo.ratedCurrent)
        insert.setNullableString(table.MATERIAL.queryIndex, wireInfo.material.name)

        return saveAssetInfo(table, insert, wireInfo, description)
    }

    /************ IEC61968 ASSETS ************/

    private fun saveAsset(table: TableAssets, insert: PreparedStatement, asset: Asset, description: String): Boolean {
        var status = true

        insert.setNullableString(table.LOCATION_MRID.queryIndex, asset.location?.mRID)
        asset.organisationRoles.forEach { status = status and saveAssociation(it, asset) }

        return status and saveIdentifiedObject(table, insert, asset, description)
    }

    private fun saveAssetContainer(table: TableAssetContainers, insert: PreparedStatement, assetContainer: AssetContainer, description: String): Boolean {
        return saveAsset(table, insert, assetContainer, description)
    }

    private fun saveAssetInfo(table: TableAssetInfo, insert: PreparedStatement, assetInfo: AssetInfo, description: String): Boolean {
        return saveIdentifiedObject(table, insert, assetInfo, description)
    }

    private fun saveAssetOrganisationRole(
        table: TableAssetOrganisationRoles,
        insert: PreparedStatement,
        assetOrganisationRole: AssetOrganisationRole,
        description: String
    ): Boolean {
        return saveOrganisationRole(table, insert, assetOrganisationRole, description)
    }

    fun save(assetOwner: AssetOwner): Boolean {
        val table = databaseTables.getTable(TableAssetOwners::class.java)
        val insert = databaseTables.getInsert(TableAssetOwners::class.java)

        return saveAssetOrganisationRole(table, insert, assetOwner, "asset owner")
    }

    private fun saveStructure(table: TableStructures, insert: PreparedStatement, structure: Structure, description: String): Boolean {
        return saveAssetContainer(table, insert, structure, description)
    }

    fun save(pole: Pole): Boolean {
        val table = databaseTables.getTable(TablePoles::class.java)
        val insert = databaseTables.getInsert(TablePoles::class.java)

        insert.setString(table.CLASSIFICATION.queryIndex, pole.classification)

        return saveStructure(table, insert, pole, "pole")
    }

    fun save(streetlight: Streetlight): Boolean {
        val table = databaseTables.getTable(TableStreetlights::class.java)
        val insert = databaseTables.getInsert(TableStreetlights::class.java)

        insert.setNullableString(table.POLE_MRID.queryIndex, streetlight.pole?.mRID)
        insert.setNullableInt(table.LIGHT_RATING.queryIndex, streetlight.lightRating)
        insert.setString(table.LAMP_KIND.queryIndex, streetlight.lampKind.name)
        return saveAsset(table, insert, streetlight, "streetlight")
    }

    /************ IEC61968 COMMON ************/

    fun save(location: Location): Boolean {
        val table = databaseTables.getTable(TableLocations::class.java)
        val insert = databaseTables.getInsert(TableLocations::class.java)

        var status = saveLocationStreetAddress(location, TableLocationStreetAddressField.mainAddress, location.mainAddress, "location main address")
        location.points.forEachIndexed { sequenceNumber, point -> status = status and savePositionPoint(location, sequenceNumber, point) }

        return status and saveIdentifiedObject(table, insert, location, "location")
    }

    private fun saveLocationStreetAddress(
        location: Location,
        field: TableLocationStreetAddressField,
        streetAddress: StreetAddress?,
        description: String
    ): Boolean {
        if (streetAddress == null)
            return true

        val table = databaseTables.getTable(TableLocationStreetAddresses::class.java)
        val insert = databaseTables.getInsert(TableLocationStreetAddresses::class.java)

        insert.setNullableString(table.LOCATION_MRID.queryIndex, location.mRID)
        insert.setNullableString(table.ADDRESS_FIELD.queryIndex, field.name)

        return saveStreetAddress(
            table,
            insert,
            streetAddress,
            "${location.mRID}-$field",
            description
        )
    }

    private fun savePositionPoint(location: Location, sequenceNumber: Int, positionPoint: PositionPoint): Boolean {
        val table = databaseTables.getTable(TablePositionPoints::class.java)
        val insert = databaseTables.getInsert(TablePositionPoints::class.java)

        insert.setNullableString(table.LOCATION_MRID.queryIndex, location.mRID)
        insert.setNullableInt(table.SEQUENCE_NUMBER.queryIndex, sequenceNumber)
        insert.setNullableDouble(table.X_POSITION.queryIndex, positionPoint.xPosition)
        insert.setNullableDouble(table.Y_POSITION.queryIndex, positionPoint.yPosition)

        return tryExecuteSingleUpdate(
            insert,
            "${location.mRID}-point$sequenceNumber",
            "position point"
        )
    }

    private fun saveStreetAddress(
        table: TableStreetAddresses,
        insert: PreparedStatement,
        streetAddress: StreetAddress,
        id: String,
        description: String
    ): Boolean {
        insert.setNullableString(table.POSTAL_CODE.queryIndex, streetAddress.postalCode)

        return saveTownDetail(table, insert, streetAddress.townDetail, id, description)
    }

    private fun saveTownDetail(table: TableTownDetails, insert: PreparedStatement, townDetail: TownDetail?, id: String, description: String): Boolean {
        insert.setNullableString(table.TOWN_NAME.queryIndex, townDetail?.name)
        insert.setNullableString(table.STATE_OR_PROVINCE.queryIndex, townDetail?.stateOrProvince)

        return tryExecuteSingleUpdate(insert, id, description)
    }

    /************ IEC61968 METERING ************/

    private fun saveEndDevice(table: TableEndDevices, insert: PreparedStatement, endDevice: EndDevice, description: String): Boolean {
        insert.setNullableString(table.CUSTOMER_MRID.queryIndex, endDevice.customerMRID)
        insert.setNullableString(table.SERVICE_LOCATION_MRID.queryIndex, endDevice.serviceLocation?.mRID)

        var status = true
        endDevice.usagePoints.forEach { status = status and saveAssociation(it, endDevice) }

        return status and saveAssetContainer(table, insert, endDevice, description)
    }

    fun save(meter: Meter): Boolean {
        val table = databaseTables.getTable(TableMeters::class.java)
        val insert = databaseTables.getInsert(TableMeters::class.java)

        return saveEndDevice(table, insert, meter, "meter")
    }

    fun save(usagePoint: UsagePoint): Boolean {
        val table = databaseTables.getTable(TableUsagePoints::class.java)
        val insert = databaseTables.getInsert(TableUsagePoints::class.java)

        insert.setNullableString(table.LOCATION_MRID.queryIndex, usagePoint.usagePointLocation?.mRID)
        insert.setBoolean(table.IS_VIRTUAL.queryIndex, usagePoint.isVirtual)
        insert.setNullableString(table.CONNECTION_CATEGORY.queryIndex, usagePoint.connectionCategory)

        var status = true
        usagePoint.equipment.forEach { status = status and saveAssociation(it, usagePoint) }

        return status and saveIdentifiedObject(table, insert, usagePoint, "usage point")
    }

    /************ IEC61968 OPERATIONS ************/

    fun save(operationalRestriction: OperationalRestriction): Boolean {
        val table = databaseTables.getTable(TableOperationalRestrictions::class.java)
        val insert = databaseTables.getInsert(TableOperationalRestrictions::class.java)

        var status = true
        operationalRestriction.equipment.forEach { status = status and saveAssociation(it, operationalRestriction) }

        return status and saveDocument(table, insert, operationalRestriction, "operational restriction")
    }

    /************ IEC61970 AUXILIARY EQUIPMENT ************/

    private fun saveAuxiliaryEquipment(
        table: TableAuxiliaryEquipment,
        insert: PreparedStatement,
        auxiliaryEquipment: AuxiliaryEquipment,
        description: String
    ): Boolean {
        insert.setNullableString(table.TERMINAL_MRID.queryIndex, auxiliaryEquipment.terminal?.mRID)

        return saveEquipment(table, insert, auxiliaryEquipment, description)
    }

    fun save(faultIndicator: FaultIndicator): Boolean {
        val table = databaseTables.getTable(TableFaultIndicators::class.java)
        val insert = databaseTables.getInsert(TableFaultIndicators::class.java)

        return saveAuxiliaryEquipment(table, insert, faultIndicator, "fault indicator")
    }

    /************ IEC61970 CORE ************/

    private fun saveAcDcTerminal(table: TableAcDcTerminals, insert: PreparedStatement, acDcTerminal: AcDcTerminal, description: String): Boolean {
        return saveIdentifiedObject(table, insert, acDcTerminal, description)
    }

    fun save(baseVoltage: BaseVoltage): Boolean {
        val table = databaseTables.getTable(TableBaseVoltages::class.java)
        val insert = databaseTables.getInsert(TableBaseVoltages::class.java)

        insert.setInt(table.NOMINAL_VOLTAGE.queryIndex, baseVoltage.nominalVoltage)

        return saveIdentifiedObject(table, insert, baseVoltage, "base voltage")
    }

    private fun saveConductingEquipment(
        table: TableConductingEquipment,
        insert: PreparedStatement,
        conductingEquipment: ConductingEquipment,
        description: String
    ): Boolean {
        insert.setNullableString(table.BASE_VOLTAGE_MRID.queryIndex, conductingEquipment.baseVoltage?.mRID)

        return saveEquipment(table, insert, conductingEquipment, description)
    }

    fun save(connectivityNode: ConnectivityNode): Boolean {
        val table = databaseTables.getTable(TableConnectivityNodes::class.java)
        val insert = databaseTables.getInsert(TableConnectivityNodes::class.java)

        return saveIdentifiedObject(table, insert, connectivityNode, "connectivity node")
    }

    private fun saveConnectivityNodeContainer(
        table: TableConnectivityNodeContainers,
        insert: PreparedStatement,
        connectivityNodeContainer: ConnectivityNodeContainer,
        description: String
    ): Boolean {
        return savePowerSystemResource(table, insert, connectivityNodeContainer, description)
    }

    private fun saveEquipment(table: TableEquipment, insert: PreparedStatement, equipment: Equipment, description: String): Boolean {
        insert.setBoolean(table.NORMALLY_IN_SERVICE.queryIndex, equipment.normallyInService)
        insert.setBoolean(table.IN_SERVICE.queryIndex, equipment.inService)

        var status = true
        equipment.containers.forEach {
            if (it !is Feeder)
                status = status and saveAssociation(equipment, it)
        }

        return status and savePowerSystemResource(table, insert, equipment, description)
    }

    private fun saveEquipmentContainer(
        table: TableEquipmentContainers,
        insert: PreparedStatement,
        equipmentContainer: EquipmentContainer,
        description: String
    ): Boolean {
        return saveConnectivityNodeContainer(table, insert, equipmentContainer, description)
    }

    fun save(feeder: Feeder): Boolean {
        val table = databaseTables.getTable(TableFeeders::class.java)
        val insert = databaseTables.getInsert(TableFeeders::class.java)

        insert.setNullableString(table.NORMAL_HEAD_TERMINAL_MRID.queryIndex, feeder.normalHeadTerminal?.mRID)
        insert.setNullableString(
            table.NORMAL_ENERGIZING_SUBSTATION_MRID.queryIndex,
            feeder.normalEnergizingSubstation?.mRID
        )

        return saveEquipmentContainer(table, insert, feeder, "feeder")
    }

    fun save(geographicalRegion: GeographicalRegion): Boolean {
        val table = databaseTables.getTable(TableGeographicalRegions::class.java)
        val insert = databaseTables.getInsert(TableGeographicalRegions::class.java)

        return saveIdentifiedObject(table, insert, geographicalRegion, "geographical region")
    }

    private fun savePowerSystemResource(
        table: TablePowerSystemResources,
        insert: PreparedStatement,
        powerSystemResource: PowerSystemResource,
        description: String
    ): Boolean {
        insert.setNullableString(table.LOCATION_MRID.queryIndex, powerSystemResource.location?.mRID)
        insert.setInt(table.NUM_CONTROLS.queryIndex, powerSystemResource.numControls)

        return saveIdentifiedObject(table, insert, powerSystemResource, description)
    }

    fun save(site: Site): Boolean {
        val table = databaseTables.getTable(TableSites::class.java)
        val insert = databaseTables.getInsert(TableSites::class.java)

        return saveEquipmentContainer(table, insert, site, "site")
    }

    fun save(subGeographicalRegion: SubGeographicalRegion): Boolean {
        val table = databaseTables.getTable(TableSubGeographicalRegions::class.java)
        val insert = databaseTables.getInsert(TableSubGeographicalRegions::class.java)

        insert.setNullableString(
            table.GEOGRAPHICAL_REGION_MRID.queryIndex,
            subGeographicalRegion.geographicalRegion?.mRID
        )

        return saveIdentifiedObject(table, insert, subGeographicalRegion, "sub-geographical region")
    }

    fun save(substation: Substation): Boolean {
        val table = databaseTables.getTable(TableSubstations::class.java)
        val insert = databaseTables.getInsert(TableSubstations::class.java)

        insert.setNullableString(table.SUB_GEOGRAPHICAL_REGION_MRID.queryIndex, substation.subGeographicalRegion?.mRID)

        return saveEquipmentContainer(table, insert, substation, "substation")
    }

    fun save(terminal: Terminal): Boolean {
        val table = databaseTables.getTable(TableTerminals::class.java)
        val insert = databaseTables.getInsert(TableTerminals::class.java)

        insert.setNullableString(table.CONDUCTING_EQUIPMENT_MRID.queryIndex, terminal.conductingEquipment?.mRID)
        insert.setInt(table.SEQUENCE_NUMBER.queryIndex, terminal.sequenceNumber)
        insert.setNullableString(table.CONNECTIVITY_NODE_MRID.queryIndex, terminal.connectivityNodeId())
        insert.setNullableString(table.PHASES.queryIndex, terminal.phases.name)

        return saveAcDcTerminal(table, insert, terminal, "terminal")
    }

    /************ IEC61970 WIRES ************/

    fun save(equivalentBranch: EquivalentBranch): Boolean {
        val table = databaseTables.getTable(TableEquivalentBranches::class.java)
        val insert = databaseTables.getInsert(TableEquivalentBranches::class.java)

        insert.setNullableDouble(table.NEGATIVE_R12.queryIndex, equivalentBranch.negativeR12)
        insert.setNullableDouble(table.NEGATIVE_R21.queryIndex, equivalentBranch.negativeR21)
        insert.setNullableDouble(table.NEGATIVE_X12.queryIndex, equivalentBranch.negativeX12)
        insert.setNullableDouble(table.NEGATIVE_X21.queryIndex, equivalentBranch.negativeX21)
        insert.setNullableDouble(table.POSITIVE_R12.queryIndex, equivalentBranch.positiveR12)
        insert.setNullableDouble(table.POSITIVE_R21.queryIndex, equivalentBranch.positiveR21)
        insert.setNullableDouble(table.POSITIVE_X12.queryIndex, equivalentBranch.positiveX12)
        insert.setNullableDouble(table.POSITIVE_X21.queryIndex, equivalentBranch.positiveX21)
        insert.setNullableDouble(table.R.queryIndex, equivalentBranch.r)
        insert.setNullableDouble(table.R21.queryIndex, equivalentBranch.r21)
        insert.setNullableDouble(table.X.queryIndex, equivalentBranch.x)
        insert.setNullableDouble(table.X21.queryIndex, equivalentBranch.x21)
        insert.setNullableDouble(table.ZERO_R12.queryIndex, equivalentBranch.zeroR12)
        insert.setNullableDouble(table.ZERO_R21.queryIndex, equivalentBranch.zeroR21)
        insert.setNullableDouble(table.ZERO_X12.queryIndex, equivalentBranch.zeroX12)
        insert.setNullableDouble(table.ZERO_X21.queryIndex, equivalentBranch.zeroX21)

        return saveEquivalentEquipment(table, insert, equivalentBranch, "equivalent branch")
    }

    fun saveEquivalentEquipment(
        table: TableEquivalentEquipment,
        insert: PreparedStatement,
        equivalentEquipment: EquivalentEquipment,
        description: String
    ): Boolean =
        saveConductingEquipment(table, insert, equivalentEquipment, description)

    /************ IEC61970 WIRES GENERATION PRODUCTION************/

    fun save(batteryUnit: BatteryUnit): Boolean {
        val table = databaseTables.getTable(TableBatteryUnit::class.java)
        val insert = databaseTables.getInsert(TableBatteryUnit::class.java)

        insert.setString(table.BATTERY_STATE.queryIndex, batteryUnit.batteryState.name)
        insert.setNullableLong(table.RATED_E.queryIndex, batteryUnit.ratedE)
        insert.setNullableLong(table.STORED_E.queryIndex, batteryUnit.storedE)

        return savePowerElectronicsUnit(table, insert, batteryUnit, "battery unit")
    }

    fun save(photoVoltaicUnit: PhotoVoltaicUnit): Boolean {
        val table = databaseTables.getTable(TablePhotoVoltaicUnit::class.java)
        val insert = databaseTables.getInsert(TablePhotoVoltaicUnit::class.java)

        return savePowerElectronicsUnit(table, insert, photoVoltaicUnit, "photo voltaic unit")
    }

    fun savePowerElectronicsUnit(
        table: TablePowerElectronicsUnit,
        insert: PreparedStatement,
        powerElectronicsUnit: PowerElectronicsUnit,
        description: String
    ): Boolean {
        insert.setNullableString(table.POWER_ELECTRONICS_CONNECTION_MRID.queryIndex, powerElectronicsUnit.powerElectronicsConnection?.mRID)
        insert.setNullableInt(table.MAX_P.queryIndex, powerElectronicsUnit.maxP)
        insert.setNullableInt(table.MIN_P.queryIndex, powerElectronicsUnit.minP)

        return saveEquipment(table, insert, powerElectronicsUnit, description)
    }

    fun save(powerElectronicsWindUnit: PowerElectronicsWindUnit): Boolean {
        val table = databaseTables.getTable(TablePowerElectronicsWindUnit::class.java)
        val insert = databaseTables.getInsert(TablePowerElectronicsWindUnit::class.java)

        return savePowerElectronicsUnit(table, insert, powerElectronicsWindUnit, "power electronics wind unit")
    }

    /************ IEC61970 WIRES ************/

    fun save(acLineSegment: AcLineSegment): Boolean {
        val table = databaseTables.getTable(TableAcLineSegments::class.java)
        val insert = databaseTables.getInsert(TableAcLineSegments::class.java)

        insert.setNullableString(
            table.PER_LENGTH_SEQUENCE_IMPEDANCE_MRID.queryIndex,
            acLineSegment.perLengthSequenceImpedance?.mRID
        )

        return saveConductor(table, insert, acLineSegment, "AC line segment")
    }

    fun save(breaker: Breaker): Boolean {
        val table = databaseTables.getTable(TableBreakers::class.java)
        val insert = databaseTables.getInsert(TableBreakers::class.java)

        return saveProtectedSwitch(table, insert, breaker, "breaker")
    }

    fun save(loadBreakSwitch: LoadBreakSwitch): Boolean {
        val table = databaseTables.getTable(TableLoadBreakSwitches::class.java)
        val insert = databaseTables.getInsert(TableLoadBreakSwitches::class.java)

        return saveProtectedSwitch(table, insert, loadBreakSwitch, "load break switch")
    }

    fun save(busbarSection: BusbarSection): Boolean {
        val table = databaseTables.getTable(TableBusbarSections::class.java)
        val insert = databaseTables.getInsert(TableBusbarSections::class.java)

        return saveConnector(table, insert, busbarSection, "busbar section")
    }

    private fun saveConductor(table: TableConductors, insert: PreparedStatement, conductor: Conductor, description: String): Boolean {
        insert.setNullableDouble(table.LENGTH.queryIndex, conductor.length)
        insert.setNullableString(table.WIRE_INFO_MRID.queryIndex, conductor.assetInfo?.mRID)

        return saveConductingEquipment(table, insert, conductor, description)
    }

    private fun saveConnector(table: TableConnectors, insert: PreparedStatement, connector: Connector, description: String): Boolean {
        return saveConductingEquipment(table, insert, connector, description)
    }

    fun save(disconnector: Disconnector): Boolean {
        val table = databaseTables.getTable(TableDisconnectors::class.java)
        val insert = databaseTables.getInsert(TableDisconnectors::class.java)

        return saveSwitch(table, insert, disconnector, "disconnector")
    }

    private fun saveEnergyConnection(
        table: TableEnergyConnections,
        insert: PreparedStatement,
        energyConnection: EnergyConnection,
        description: String
    ): Boolean {
        return saveConductingEquipment(table, insert, energyConnection, description)
    }

    fun save(energyConsumer: EnergyConsumer): Boolean {
        val table = databaseTables.getTable(TableEnergyConsumers::class.java)
        val insert = databaseTables.getInsert(TableEnergyConsumers::class.java)

        insert.setNullableInt(table.CUSTOMER_COUNT.queryIndex, energyConsumer.customerCount)
        insert.setBoolean(table.GROUNDED.queryIndex, energyConsumer.grounded)
        insert.setNullableDouble(table.P.queryIndex, energyConsumer.p)
        insert.setNullableDouble(table.Q.queryIndex, energyConsumer.q)
        insert.setNullableDouble(table.P_FIXED.queryIndex, energyConsumer.pFixed)
        insert.setNullableDouble(table.Q_FIXED.queryIndex, energyConsumer.qFixed)
        insert.setNullableString(table.PHASE_CONNECTION.queryIndex, energyConsumer.phaseConnection.name)

        return saveEnergyConnection(table, insert, energyConsumer, "energy consumer")
    }

    fun save(energyConsumerPhase: EnergyConsumerPhase): Boolean {
        val table = databaseTables.getTable(TableEnergyConsumerPhases::class.java)
        val insert = databaseTables.getInsert(TableEnergyConsumerPhases::class.java)

        insert.setNullableString(table.ENERGY_CONSUMER_MRID.queryIndex, energyConsumerPhase.energyConsumer?.mRID)
        insert.setNullableString(table.PHASE.queryIndex, energyConsumerPhase.phase.name)
        insert.setNullableDouble(table.P.queryIndex, energyConsumerPhase.p)
        insert.setNullableDouble(table.Q.queryIndex, energyConsumerPhase.q)
        insert.setNullableDouble(table.P_FIXED.queryIndex, energyConsumerPhase.pFixed)
        insert.setNullableDouble(table.Q_FIXED.queryIndex, energyConsumerPhase.qFixed)

        return savePowerSystemResource(table, insert, energyConsumerPhase, "energy consumer phase")
    }

    fun save(energySource: EnergySource): Boolean {
        val table = databaseTables.getTable(TableEnergySources::class.java)
        val insert = databaseTables.getInsert(TableEnergySources::class.java)

        insert.setNullableDouble(table.ACTIVE_POWER.queryIndex, energySource.activePower)
        insert.setNullableDouble(table.REACTIVE_POWER.queryIndex, energySource.reactivePower)
        insert.setNullableDouble(table.VOLTAGE_ANGLE.queryIndex, energySource.voltageAngle)
        insert.setNullableDouble(table.VOLTAGE_MAGNITUDE.queryIndex, energySource.voltageMagnitude)
        insert.setNullableDouble(table.P_MAX.queryIndex, energySource.pMax)
        insert.setNullableDouble(table.P_MIN.queryIndex, energySource.pMin)
        insert.setNullableDouble(table.R.queryIndex, energySource.r)
        insert.setNullableDouble(table.R0.queryIndex, energySource.r0)
        insert.setNullableDouble(table.RN.queryIndex, energySource.rn)
        insert.setNullableDouble(table.X.queryIndex, energySource.x)
        insert.setNullableDouble(table.X0.queryIndex, energySource.x0)
        insert.setNullableDouble(table.XN.queryIndex, energySource.xn)

        return saveEnergyConnection(table, insert, energySource, "energy source")
    }

    fun save(energySourcePhase: EnergySourcePhase): Boolean {
        val table = databaseTables.getTable(TableEnergySourcePhases::class.java)
        val insert = databaseTables.getInsert(TableEnergySourcePhases::class.java)

        insert.setNullableString(table.ENERGY_SOURCE_MRID.queryIndex, energySourcePhase.energySource?.mRID)
        insert.setNullableString(table.PHASE.queryIndex, energySourcePhase.phase.name)

        return savePowerSystemResource(table, insert, energySourcePhase, "energy source phase")
    }

    fun save(fuse: Fuse): Boolean {
        val table = databaseTables.getTable(TableFuses::class.java)
        val insert = databaseTables.getInsert(TableFuses::class.java)

        return saveSwitch(table, insert, fuse, "fuse")
    }

    fun save(jumper: Jumper): Boolean {
        val table = databaseTables.getTable(TableJumpers::class.java)
        val insert = databaseTables.getInsert(TableJumpers::class.java)

        return saveSwitch(table, insert, jumper, "jumper")
    }

    fun save(junction: Junction): Boolean {
        val table = databaseTables.getTable(TableJunctions::class.java)
        val insert = databaseTables.getInsert(TableJunctions::class.java)

        return saveConnector(table, insert, junction, "junction")
    }

    private fun saveLine(table: TableLines, insert: PreparedStatement, line: Line, description: String): Boolean {
        return saveEquipmentContainer(table, insert, line, description)
    }

    fun save(linearShuntCompensator: LinearShuntCompensator): Boolean {
        val table = databaseTables.getTable(TableLinearShuntCompensators::class.java)
        val insert = databaseTables.getInsert(TableLinearShuntCompensators::class.java)

        insert.setNullableDouble(table.B0_PER_SECTION.queryIndex, linearShuntCompensator.b0PerSection)
        insert.setNullableDouble(table.B_PER_SECTION.queryIndex, linearShuntCompensator.bPerSection)
        insert.setNullableDouble(table.G0_PER_SECTION.queryIndex, linearShuntCompensator.g0PerSection)
        insert.setNullableDouble(table.G_PER_SECTION.queryIndex, linearShuntCompensator.gPerSection)

        return saveShuntCompensator(table, insert, linearShuntCompensator, "linear shunt compensator")
    }

    private fun savePerLengthImpedance(
        table: TablePerLengthImpedances,
        insert: PreparedStatement,
        perLengthImpedance: PerLengthImpedance,
        description: String
    ): Boolean {
        return savePerLengthLineParameter(table, insert, perLengthImpedance, description)
    }

    private fun savePerLengthLineParameter(
        table: TablePerLengthLineParameters,
        insert: PreparedStatement,
        perLengthLineParameter: PerLengthLineParameter,
        description: String
    ): Boolean {
        return saveIdentifiedObject(table, insert, perLengthLineParameter, description)
    }

    fun save(perLengthSequenceImpedance: PerLengthSequenceImpedance): Boolean {
        val table = databaseTables.getTable(TablePerLengthSequenceImpedances::class.java)
        val insert = databaseTables.getInsert(TablePerLengthSequenceImpedances::class.java)

        insert.setNullableDouble(table.R.queryIndex, perLengthSequenceImpedance.r)
        insert.setNullableDouble(table.X.queryIndex, perLengthSequenceImpedance.x)
        insert.setNullableDouble(table.R0.queryIndex, perLengthSequenceImpedance.r0)
        insert.setNullableDouble(table.X0.queryIndex, perLengthSequenceImpedance.x0)
        insert.setNullableDouble(table.BCH.queryIndex, perLengthSequenceImpedance.bch)
        insert.setNullableDouble(table.GCH.queryIndex, perLengthSequenceImpedance.gch)
        insert.setNullableDouble(table.B0CH.queryIndex, perLengthSequenceImpedance.b0ch)
        insert.setNullableDouble(table.G0CH.queryIndex, perLengthSequenceImpedance.g0ch)

        return savePerLengthImpedance(table, insert, perLengthSequenceImpedance, "per length sequence impedance")
    }

    fun save(powerElectronicsConnection: PowerElectronicsConnection): Boolean {
        val table = databaseTables.getTable(TablePowerElectronicsConnection::class.java)
        val insert = databaseTables.getInsert(TablePowerElectronicsConnection::class.java)

        insert.setNullableInt(table.MAX_I_FAULT.queryIndex, powerElectronicsConnection.maxIFault)
        insert.setNullableDouble(table.MAX_Q.queryIndex, powerElectronicsConnection.maxQ)
        insert.setNullableDouble(table.MIN_Q.queryIndex, powerElectronicsConnection.minQ)
        insert.setNullableDouble(table.P.queryIndex, powerElectronicsConnection.p)
        insert.setNullableDouble(table.Q.queryIndex, powerElectronicsConnection.q)
        insert.setNullableInt(table.RATED_S.queryIndex, powerElectronicsConnection.ratedS)
        insert.setNullableInt(table.RATED_U.queryIndex, powerElectronicsConnection.ratedU)

        return saveRegulatingCondEq(table, insert, powerElectronicsConnection, "power electronics connection")
    }

    fun save(powerElectronicsConnectionPhase: PowerElectronicsConnectionPhase): Boolean {
        val table = databaseTables.getTable(TablePowerElectronicsConnectionPhases::class.java)
        val insert = databaseTables.getInsert(TablePowerElectronicsConnectionPhases::class.java)

        insert.setNullableString(table.POWER_ELECTRONICS_CONNECTION_MRID.queryIndex, powerElectronicsConnectionPhase.powerElectronicsConnection?.mRID)
        insert.setNullableDouble(table.P.queryIndex, powerElectronicsConnectionPhase.p)
        insert.setString(table.PHASE.queryIndex, powerElectronicsConnectionPhase.phase.name)
        insert.setNullableDouble(table.Q.queryIndex, powerElectronicsConnectionPhase.q)

        return savePowerSystemResource(table, insert, powerElectronicsConnectionPhase, "power electronics connection phase")
    }

    fun save(powerTransformer: PowerTransformer): Boolean {
        val table = databaseTables.getTable(TablePowerTransformers::class.java)
        val insert = databaseTables.getInsert(TablePowerTransformers::class.java)

        insert.setNullableString(table.VECTOR_GROUP.queryIndex, powerTransformer.vectorGroup.name)
        insert.setNullableDouble(table.TRANSFORMER_UTILISATION.queryIndex, powerTransformer.transformerUtilisation)
        insert.setString(table.POWER_TRANSFORMER_INFO_MRID.queryIndex, powerTransformer.assetInfo?.mRID)

        return saveConductingEquipment(table, insert, powerTransformer, "power transformer")
    }

    fun save(powerTransformerEnd: PowerTransformerEnd): Boolean {
        val table = databaseTables.getTable(TablePowerTransformerEnds::class.java)
        val insert = databaseTables.getInsert(TablePowerTransformerEnds::class.java)

        insert.setNullableString(table.POWER_TRANSFORMER_MRID.queryIndex, powerTransformerEnd.powerTransformer?.mRID)
        insert.setNullableString(table.CONNECTION_KIND.queryIndex, powerTransformerEnd.connectionKind.name)
        insert.setNullableInt(table.PHASE_ANGLE_CLOCK.queryIndex, powerTransformerEnd.phaseAngleClock)
        insert.setNullableDouble(table.B.queryIndex, powerTransformerEnd.b)
        insert.setNullableDouble(table.B0.queryIndex, powerTransformerEnd.b0)
        insert.setNullableDouble(table.G.queryIndex, powerTransformerEnd.g)
        insert.setNullableDouble(table.G0.queryIndex, powerTransformerEnd.g0)
        insert.setNullableDouble(table.R.queryIndex, powerTransformerEnd.r)
        insert.setNullableDouble(table.R0.queryIndex, powerTransformerEnd.r0)
        insert.setNullableInt(table.RATED_S.queryIndex, powerTransformerEnd.ratedS)
        insert.setNullableInt(table.RATED_U.queryIndex, powerTransformerEnd.ratedU)
        insert.setNullableDouble(table.X.queryIndex, powerTransformerEnd.x)
        insert.setNullableDouble(table.X0.queryIndex, powerTransformerEnd.x0)

        return saveTransformerEnd(table, insert, powerTransformerEnd, "power transformer end")
    }

    private fun saveProtectedSwitch(table: TableProtectedSwitches, insert: PreparedStatement, protectedSwitch: ProtectedSwitch, description: String): Boolean {
        return saveSwitch(table, insert, protectedSwitch, description)
    }

    fun save(ratioTapChanger: RatioTapChanger): Boolean {
        val table = databaseTables.getTable(TableRatioTapChangers::class.java)
        val insert = databaseTables.getInsert(TableRatioTapChangers::class.java)

        insert.setNullableString(table.TRANSFORMER_END_MRID.queryIndex, ratioTapChanger.transformerEnd?.mRID)
        insert.setNullableDouble(table.STEP_VOLTAGE_INCREMENT.queryIndex, ratioTapChanger.stepVoltageIncrement)

        return saveTapChanger(table, insert, ratioTapChanger, "ratio tap changer")
    }

    fun save(recloser: Recloser): Boolean {
        val table = databaseTables.getTable(TableReclosers::class.java)
        val insert = databaseTables.getInsert(TableReclosers::class.java)

        return saveProtectedSwitch(table, insert, recloser, "recloser")
    }

    private fun saveRegulatingCondEq(
        table: TableRegulatingCondEq,
        insert: PreparedStatement,
        regulatingCondEq: RegulatingCondEq,
        description: String
    ): Boolean {
        insert.setBoolean(table.CONTROL_ENABLED.queryIndex, regulatingCondEq.controlEnabled)

        return saveEnergyConnection(table, insert, regulatingCondEq, description)
    }

    private fun saveShuntCompensator(
        table: TableShuntCompensators,
        insert: PreparedStatement,
        shuntCompensator: ShuntCompensator,
        description: String
    ): Boolean {
        insert.setBoolean(table.GROUNDED.queryIndex, shuntCompensator.grounded)
        insert.setNullableInt(table.NOM_U.queryIndex, shuntCompensator.nomU)
        insert.setNullableString(table.PHASE_CONNECTION.queryIndex, shuntCompensator.phaseConnection.name)
        insert.setNullableDouble(table.SECTIONS.queryIndex, shuntCompensator.sections)

        return saveRegulatingCondEq(table, insert, shuntCompensator, description)
    }

    private fun saveSwitch(table: TableSwitches, insert: PreparedStatement, switch: Switch, description: String): Boolean {
        insert.setInt(table.NORMAL_OPEN.queryIndex, switch.normalOpen)
        insert.setInt(table.OPEN.queryIndex, switch.open)

        return saveConductingEquipment(table, insert, switch, description)
    }

    private fun saveTapChanger(table: TableTapChangers, insert: PreparedStatement, tapChanger: TapChanger, description: String): Boolean {
        insert.setBoolean(table.CONTROL_ENABLED.queryIndex, tapChanger.controlEnabled)
        insert.setNullableInt(table.HIGH_STEP.queryIndex, tapChanger.highStep)
        insert.setNullableInt(table.LOW_STEP.queryIndex, tapChanger.lowStep)
        insert.setNullableInt(table.NEUTRAL_STEP.queryIndex, tapChanger.neutralStep)
        insert.setNullableInt(table.NEUTRAL_U.queryIndex, tapChanger.neutralU)
        insert.setNullableInt(table.NORMAL_STEP.queryIndex, tapChanger.normalStep)
        insert.setNullableDouble(table.STEP.queryIndex, tapChanger.step)

        return savePowerSystemResource(table, insert, tapChanger, description)
    }

    private fun saveTransformerEnd(
        table: TableTransformerEnds,
        insert: PreparedStatement,
        transformerEnd: TransformerEnd,
        description: String
    ): Boolean {
        insert.setInt(table.END_NUMBER.queryIndex, transformerEnd.endNumber)
        insert.setNullableString(table.TERMINAL_MRID.queryIndex, transformerEnd.terminal?.mRID)
        insert.setNullableString(table.BASE_VOLTAGE_MRID.queryIndex, transformerEnd.baseVoltage?.mRID)
        insert.setBoolean(table.GROUNDED.queryIndex, transformerEnd.grounded)
        insert.setNullableDouble(table.R_GROUND.queryIndex, transformerEnd.rGround)
        insert.setNullableDouble(table.X_GROUND.queryIndex, transformerEnd.xGround)
        insert.setNullableString(table.STAR_IMPEDANCE_MRID.queryIndex, transformerEnd.starImpedance?.mRID)

        return saveIdentifiedObject(table, insert, transformerEnd, description)
    }

    fun save(transformerStarImpedance: TransformerStarImpedance): Boolean {
        val table = databaseTables.getTable(TableTransformerStarImpedance::class.java)
        val insert = databaseTables.getInsert(TableTransformerStarImpedance::class.java)

        insert.setNullableDouble(table.R.queryIndex, transformerStarImpedance.r)
        insert.setNullableDouble(table.R0.queryIndex, transformerStarImpedance.r0)
        insert.setNullableDouble(table.X.queryIndex, transformerStarImpedance.x)
        insert.setNullableDouble(table.X0.queryIndex, transformerStarImpedance.x0)
        insert.setNullableString(table.TRANSFORMER_END_INFO_MRID.queryIndex, transformerStarImpedance.transformerEndInfo?.mRID)

        return saveIdentifiedObject(table, insert, transformerStarImpedance, "transformer star impedance")
    }

    /************ IEC61970 InfIEC61970 ************/

    fun save(circuit: Circuit): Boolean {
        val table = databaseTables.getTable(TableCircuits::class.java)
        val insert = databaseTables.getInsert(TableCircuits::class.java)

        insert.setNullableString(table.LOOP_MRID.queryIndex, circuit.loop?.mRID)

        var status = true
        circuit.endSubstations.forEach { status = status and saveAssociation(circuit, it) }
        circuit.endTerminals.forEach { status = status and saveAssociation(circuit, it) }

        return status and saveLine(table, insert, circuit, "circuit")
    }

    fun save(loop: Loop): Boolean {
        val table = databaseTables.getTable(TableLoops::class.java)
        val insert = databaseTables.getInsert(TableLoops::class.java)

        var status = true
        loop.energizingSubstations.forEach { status = status and saveAssociation(loop, it, LoopSubstationRelationship.SUBSTATION_ENERGIZES_LOOP) }
        loop.substations.forEach { status = status and saveAssociation(loop, it, LoopSubstationRelationship.LOOP_ENERGIZES_SUBSTATION) }

        return status and saveIdentifiedObject(table, insert, loop, "loop")
    }

    /************ IEC61970 MEAS ************/
    private fun saveMeasurement(
        table: TableMeasurements,
        insert: PreparedStatement,
        measurement: Measurement,
        description: String
    ): Boolean {
        insert.setNullableString(table.POWER_SYSTEM_RESOURCE_MRID.queryIndex, measurement.powerSystemResourceMRID)
        insert.setNullableString(table.REMOTE_SOURCE_MRID.queryIndex, measurement.remoteSource?.mRID)
        insert.setNullableString(table.TERMINAL_MRID.queryIndex, measurement.terminalMRID)
        insert.setString(table.PHASES.queryIndex, measurement.phases.name)
        insert.setString(table.UNIT_SYMBOL.queryIndex, measurement.unitSymbol.name)
        return saveIdentifiedObject(table, insert, measurement, description)
    }

    fun save(analog: Analog): Boolean {
        val table = databaseTables.getTable(TableAnalogs::class.java)
        val insert = databaseTables.getInsert(TableAnalogs::class.java)

        insert.setBoolean(table.POSITIVE_FLOW_IN.queryIndex, analog.positiveFlowIn)

        return saveMeasurement(table, insert, analog, "analog")
    }

    fun save(accumulator: Accumulator): Boolean {
        val table = databaseTables.getTable(TableAccumulators::class.java)
        val insert = databaseTables.getInsert(TableAccumulators::class.java)

        return saveMeasurement(table, insert, accumulator, "accumulator")
    }

    fun save(discrete: Discrete): Boolean {
        val table = databaseTables.getTable(TableDiscretes::class.java)
        val insert = databaseTables.getInsert(TableDiscretes::class.java)

        return saveMeasurement(table, insert, discrete, "discrete")
    }

    fun save(control: Control): Boolean {
        val table = databaseTables.getTable(TableControls::class.java)
        val insert = databaseTables.getInsert(TableControls::class.java)

        insert.setNullableString(table.POWER_SYSTEM_RESOURCE_MRID.queryIndex, control.powerSystemResourceMRID)

        return saveIoPoint(table, insert, control, "control")
    }

    private fun saveIoPoint(table: TableIoPoints, insert: PreparedStatement, ioPoint: IoPoint, description: String): Boolean {
        return saveIdentifiedObject(table, insert, ioPoint, description)
    }

    /************ IEC61970 SCADA ************/
    fun save(remoteControl: RemoteControl): Boolean {
        val table = databaseTables.getTable(TableRemoteControls::class.java)
        val insert = databaseTables.getInsert(TableRemoteControls::class.java)

        insert.setNullableString(table.CONTROL_MRID.queryIndex, remoteControl.control?.mRID)

        return saveRemotePoint(table, insert, remoteControl, "remote control")
    }

    private fun saveRemotePoint(table: TableRemotePoints, insert: PreparedStatement, remotePoint: RemotePoint, description: String): Boolean {
        return saveIdentifiedObject(table, insert, remotePoint, description)
    }

    fun save(remoteSource: RemoteSource): Boolean {
        val table = databaseTables.getTable(TableRemoteSources::class.java)
        val insert = databaseTables.getInsert(TableRemoteSources::class.java)

        insert.setNullableString(table.MEASUREMENT_MRID.queryIndex, remoteSource.measurement?.mRID)

        return saveRemotePoint(table, insert, remoteSource, "remote source")
    }

    /************ ASSOCIATIONS ************/
    private fun saveAssociation(assetOrganisationRole: AssetOrganisationRole, asset: Asset): Boolean {
        val table = databaseTables.getTable(TableAssetOrganisationRolesAssets::class.java)
        val insert = databaseTables.getInsert(TableAssetOrganisationRolesAssets::class.java)

        insert.setNullableString(table.ASSET_ORGANISATION_ROLE_MRID.queryIndex, assetOrganisationRole.mRID)
        insert.setNullableString(table.ASSET_MRID.queryIndex, asset.mRID)

        return tryExecuteSingleUpdate(
            insert,
            "${assetOrganisationRole.mRID}-to-${asset.mRID}",
            "asset organisation role to asset association"
        )
    }

    private fun saveAssociation(usagePoint: UsagePoint, endDevice: EndDevice): Boolean {
        val table = databaseTables.getTable(TableUsagePointsEndDevices::class.java)
        val insert = databaseTables.getInsert(TableUsagePointsEndDevices::class.java)

        insert.setNullableString(table.USAGE_POINT_MRID.queryIndex, usagePoint.mRID)
        insert.setNullableString(table.END_DEVICE_MRID.queryIndex, endDevice.mRID)

        return tryExecuteSingleUpdate(
            insert,
            "${usagePoint.mRID}-to-${endDevice.mRID}",
            "usage point to end device association"
        )
    }

    private fun saveAssociation(equipment: Equipment, usagePoint: UsagePoint): Boolean {
        val table = databaseTables.getTable(TableEquipmentUsagePoints::class.java)
        val insert = databaseTables.getInsert(TableEquipmentUsagePoints::class.java)

        insert.setNullableString(table.EQUIPMENT_MRID.queryIndex, equipment.mRID)
        insert.setNullableString(table.USAGE_POINT_MRID.queryIndex, usagePoint.mRID)

        return tryExecuteSingleUpdate(
            insert,
            "${equipment.mRID}-to-${usagePoint.mRID}",
            "equipment to usage point association"
        )
    }

    private fun saveAssociation(equipment: Equipment, operationalRestriction: OperationalRestriction): Boolean {
        val table = databaseTables.getTable(TableEquipmentOperationalRestrictions::class.java)
        val insert = databaseTables.getInsert(TableEquipmentOperationalRestrictions::class.java)

        insert.setNullableString(table.EQUIPMENT_MRID.queryIndex, equipment.mRID)
        insert.setNullableString(table.OPERATIONAL_RESTRICTION_MRID.queryIndex, operationalRestriction.mRID)

        return tryExecuteSingleUpdate(
            insert,
            "${equipment.mRID}-to-${operationalRestriction.mRID}",
            "equipment to operational restriction association"
        )
    }

    private fun saveAssociation(equipment: Equipment, equipmentContainer: EquipmentContainer): Boolean {
        val table = databaseTables.getTable(TableEquipmentEquipmentContainers::class.java)
        val insert = databaseTables.getInsert(TableEquipmentEquipmentContainers::class.java)

        insert.setNullableString(table.EQUIPMENT_MRID.queryIndex, equipment.mRID)
        insert.setNullableString(table.EQUIPMENT_CONTAINER_MRID.queryIndex, equipmentContainer.mRID)

        return tryExecuteSingleUpdate(
            insert,
            "${equipment.mRID}-to-${equipmentContainer.mRID}",
            "equipment to equipment container association"
        )
    }

    private fun saveAssociation(circuit: Circuit, substation: Substation): Boolean {
        val table = databaseTables.getTable(TableCircuitsSubstations::class.java)
        val insert = databaseTables.getInsert(TableCircuitsSubstations::class.java)

        insert.setNullableString(table.CIRCUIT_MRID.queryIndex, circuit.mRID)
        insert.setNullableString(table.SUBSTATION_MRID.queryIndex, substation.mRID)

        return tryExecuteSingleUpdate(
            insert,
            "${circuit.mRID}-to-${substation.mRID}",
            "circuit to substation association"
        )
    }

    private fun saveAssociation(circuit: Circuit, terminal: Terminal): Boolean {
        val table = databaseTables.getTable(TableCircuitsTerminals::class.java)
        val insert = databaseTables.getInsert(TableCircuitsTerminals::class.java)

        insert.setNullableString(table.CIRCUIT_MRID.queryIndex, circuit.mRID)
        insert.setNullableString(table.TERMINAL_MRID.queryIndex, terminal.mRID)

        return tryExecuteSingleUpdate(
            insert,
            "${circuit.mRID}-to-${terminal.mRID}",
            "circuit to terminal association"
        )
    }

    private fun saveAssociation(loop: Loop, substation: Substation, relationship: LoopSubstationRelationship): Boolean {
        val table = databaseTables.getTable(TableLoopsSubstations::class.java)
        val insert = databaseTables.getInsert(TableLoopsSubstations::class.java)

        insert.setNullableString(table.LOOP_MRID.queryIndex, loop.mRID)
        insert.setNullableString(table.SUBSTATION_MRID.queryIndex, substation.mRID)
        insert.setNullableString(table.RELATIONSHIP.queryIndex, relationship.name)

        return tryExecuteSingleUpdate(
            insert,
            "${loop.mRID}-to-${substation.mRID}",
            "loop to substation association"
        )
    }
}
