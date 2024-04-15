/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.database.sqlite.network

import com.zepben.evolve.cim.iec61968.assetinfo.*
import com.zepben.evolve.cim.iec61968.assets.*
import com.zepben.evolve.cim.iec61968.common.*
import com.zepben.evolve.cim.iec61968.infiec61968.infassetinfo.CurrentTransformerInfo
import com.zepben.evolve.cim.iec61968.infiec61968.infassetinfo.PotentialTransformerInfo
import com.zepben.evolve.cim.iec61968.infiec61968.infassetinfo.RelayInfo
import com.zepben.evolve.cim.iec61968.metering.EndDevice
import com.zepben.evolve.cim.iec61968.metering.Meter
import com.zepben.evolve.cim.iec61968.metering.UsagePoint
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.base.auxiliaryequipment.*
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentBranch
import com.zepben.evolve.cim.iec61970.base.equivalents.EquivalentEquipment
import com.zepben.evolve.cim.iec61970.base.meas.*
import com.zepben.evolve.cim.iec61970.base.protection.*
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
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.LvFeeder
import com.zepben.evolve.cim.iec61970.infiec61970.wires.generation.production.EvChargingUnit
import com.zepben.evolve.database.sqlite.common.cim.CimWriter
import com.zepben.evolve.database.sqlite.extensions.*
import com.zepben.evolve.database.sqlite.tables.associations.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assetinfo.*
import com.zepben.evolve.database.sqlite.tables.iec61968.assets.*
import com.zepben.evolve.database.sqlite.tables.iec61968.common.*
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TableCurrentTransformerInfo
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TablePotentialTransformerInfo
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TableRecloseDelays
import com.zepben.evolve.database.sqlite.tables.iec61968.infiec61968.infassetinfo.TableRelayInfo
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableEndDevices
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableMeters
import com.zepben.evolve.database.sqlite.tables.iec61968.metering.TableUsagePoints
import com.zepben.evolve.database.sqlite.tables.iec61968.operations.TableOperationalRestrictions
import com.zepben.evolve.database.sqlite.tables.iec61970.base.auxiliaryequipment.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.core.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.equivalents.TableEquivalentBranches
import com.zepben.evolve.database.sqlite.tables.iec61970.base.equivalents.TableEquivalentEquipment
import com.zepben.evolve.database.sqlite.tables.iec61970.base.meas.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.protection.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteControls
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemotePoints
import com.zepben.evolve.database.sqlite.tables.iec61970.base.scada.TableRemoteSources
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.*
import com.zepben.evolve.database.sqlite.tables.iec61970.base.wires.generation.production.*
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableCircuits
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableLoops
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.feeder.TableLvFeeders
import com.zepben.evolve.database.sqlite.tables.iec61970.infiec61970.wires.generation.production.TableEvChargingUnits
import com.zepben.evolve.services.network.NetworkService
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * A class for writing the [NetworkService] tables to the database.
 *
 * @property databaseTables The tables available in the database.
 */
@Suppress("SameParameterValue")
class NetworkCimWriter(
    override val databaseTables: NetworkDatabaseTables
) : CimWriter(databaseTables) {

    // #######################
    // # IEC61968 Asset Info #
    // #######################

    /**
     * Save the [CableInfo] fields to [TableCableInfo].
     *
     * @param cableInfo The [CableInfo] instance to write to the database.
     *
     * @return true if the [CableInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(cableInfo: CableInfo): Boolean {
        val table = databaseTables.getTable<TableCableInfo>()
        val insert = databaseTables.getInsert<TableCableInfo>()

        return saveWireInfo(table, insert, cableInfo, "cable info")
    }

    /**
     * Save the [NoLoadTest] fields to [TableNoLoadTests].
     *
     * @param noLoadTest The [NoLoadTest] instance to write to the database.
     *
     * @return true if the [NoLoadTest] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(noLoadTest: NoLoadTest): Boolean {
        val table = databaseTables.getTable<TableNoLoadTests>()
        val insert = databaseTables.getInsert<TableNoLoadTests>()

        insert.setNullableInt(table.ENERGISED_END_VOLTAGE.queryIndex, noLoadTest.energisedEndVoltage)
        insert.setNullableDouble(table.EXCITING_CURRENT.queryIndex, noLoadTest.excitingCurrent)
        insert.setNullableDouble(table.EXCITING_CURRENT_ZERO.queryIndex, noLoadTest.excitingCurrentZero)
        insert.setNullableInt(table.LOSS.queryIndex, noLoadTest.loss)
        insert.setNullableInt(table.LOSS_ZERO.queryIndex, noLoadTest.lossZero)

        return saveTransformerTest(table, insert, noLoadTest, "no load test")
    }

    /**
     * Save the [OpenCircuitTest] fields to [TableOpenCircuitTests].
     *
     * @param openCircuitTest The [OpenCircuitTest] instance to write to the database.
     *
     * @return true if the [OpenCircuitTest] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(openCircuitTest: OpenCircuitTest): Boolean {
        val table = databaseTables.getTable<TableOpenCircuitTests>()
        val insert = databaseTables.getInsert<TableOpenCircuitTests>()

        insert.setNullableInt(table.ENERGISED_END_STEP.queryIndex, openCircuitTest.energisedEndStep)
        insert.setNullableInt(table.ENERGISED_END_VOLTAGE.queryIndex, openCircuitTest.energisedEndVoltage)
        insert.setNullableInt(table.OPEN_END_STEP.queryIndex, openCircuitTest.openEndStep)
        insert.setNullableInt(table.OPEN_END_VOLTAGE.queryIndex, openCircuitTest.openEndVoltage)
        insert.setNullableDouble(table.PHASE_SHIFT.queryIndex, openCircuitTest.phaseShift)

        return saveTransformerTest(table, insert, openCircuitTest, "open circuit test")
    }

    /**
     * Save the [OverheadWireInfo] fields to [TableOverheadWireInfo].
     *
     * @param overheadWireInfo The [OverheadWireInfo] instance to write to the database.
     *
     * @return true if the [OverheadWireInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(overheadWireInfo: OverheadWireInfo): Boolean {
        val table = databaseTables.getTable<TableOverheadWireInfo>()
        val insert = databaseTables.getInsert<TableOverheadWireInfo>()

        return saveWireInfo(table, insert, overheadWireInfo, "overhead wire info")
    }

    /**
     * Save the [PowerTransformerInfo] fields to [TablePowerTransformerInfo].
     *
     * @param powerTransformerInfo The [PowerTransformerInfo] instance to write to the database.
     *
     * @return true if the [PowerTransformerInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(powerTransformerInfo: PowerTransformerInfo): Boolean {
        val table = databaseTables.getTable<TablePowerTransformerInfo>()
        val insert = databaseTables.getInsert<TablePowerTransformerInfo>()

        return saveAssetInfo(table, insert, powerTransformerInfo, "power transformer info")
    }

    /**
     * Save the [ShortCircuitTest] fields to [TableShortCircuitTests].
     *
     * @param shortCircuitTest The [ShortCircuitTest] instance to write to the database.
     *
     * @return true if the [ShortCircuitTest] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(shortCircuitTest: ShortCircuitTest): Boolean {
        val table = databaseTables.getTable<TableShortCircuitTests>()
        val insert = databaseTables.getInsert<TableShortCircuitTests>()

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

    /**
     * Save the [ShuntCompensatorInfo] fields to [TableShuntCompensatorInfo].
     *
     * @param shuntCompensatorInfo The [ShuntCompensatorInfo] instance to write to the database.
     *
     * @return true if the [ShuntCompensatorInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(shuntCompensatorInfo: ShuntCompensatorInfo): Boolean {
        val table = databaseTables.getTable<TableShuntCompensatorInfo>()
        val insert = databaseTables.getInsert<TableShuntCompensatorInfo>()

        insert.setNullableInt(table.MAX_POWER_LOSS.queryIndex, shuntCompensatorInfo.maxPowerLoss)
        insert.setNullableInt(table.RATED_CURRENT.queryIndex, shuntCompensatorInfo.ratedCurrent)
        insert.setNullableInt(table.RATED_REACTIVE_POWER.queryIndex, shuntCompensatorInfo.ratedReactivePower)
        insert.setNullableInt(table.RATED_VOLTAGE.queryIndex, shuntCompensatorInfo.ratedVoltage)

        return saveAssetInfo(table, insert, shuntCompensatorInfo, "shunt compensator info")
    }

    /**
     * Save the [SwitchInfo] fields to [TableSwitchInfo].
     *
     * @param switchInfo The [SwitchInfo] instance to write to the database.
     *
     * @return true if the [SwitchInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(switchInfo: SwitchInfo): Boolean {
        val table = databaseTables.getTable<TableSwitchInfo>()
        val insert = databaseTables.getInsert<TableSwitchInfo>()

        insert.setNullableDouble(table.RATED_INTERRUPTING_TIME.queryIndex, switchInfo.ratedInterruptingTime)

        return saveAssetInfo(table, insert, switchInfo, "switch info")
    }

    /**
     * Save the [TransformerEndInfo] fields to [TableTransformerEndInfo].
     *
     * @param transformerEndInfo The [TransformerEndInfo] instance to write to the database.
     *
     * @return true if the [TransformerEndInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(transformerEndInfo: TransformerEndInfo): Boolean {
        val table = databaseTables.getTable<TableTransformerEndInfo>()
        val insert = databaseTables.getInsert<TableTransformerEndInfo>()

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

    /**
     * Save the [TransformerTankInfo] fields to [TableTransformerTankInfo].
     *
     * @param transformerTankInfo The [TransformerTankInfo] instance to write to the database.
     *
     * @return true if the [TransformerTankInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(transformerTankInfo: TransformerTankInfo): Boolean {
        val table = databaseTables.getTable<TableTransformerTankInfo>()
        val insert = databaseTables.getInsert<TableTransformerTankInfo>()

        insert.setNullableString(table.POWER_TRANSFORMER_INFO_MRID.queryIndex, transformerTankInfo.powerTransformerInfo?.mRID)

        return saveAssetInfo(table, insert, transformerTankInfo, "transformer tank info")
    }

    @Throws(SQLException::class)
    private fun saveTransformerTest(table: TableTransformerTest, insert: PreparedStatement, transformerTest: TransformerTest, description: String): Boolean {
        insert.setNullableInt(table.BASE_POWER.queryIndex, transformerTest.basePower)
        insert.setNullableDouble(table.TEMPERATURE.queryIndex, transformerTest.temperature)

        return saveIdentifiedObject(table, insert, transformerTest, description)
    }

    @Throws(SQLException::class)
    private fun saveWireInfo(table: TableWireInfo, insert: PreparedStatement, wireInfo: WireInfo, description: String): Boolean {
        insert.setNullableInt(table.RATED_CURRENT.queryIndex, wireInfo.ratedCurrent)
        insert.setNullableString(table.MATERIAL.queryIndex, wireInfo.material.name)

        return saveAssetInfo(table, insert, wireInfo, description)
    }

    // ###################
    // # IEC61968 Assets #
    // ###################

    @Throws(SQLException::class)
    private fun saveAsset(table: TableAssets, insert: PreparedStatement, asset: Asset, description: String): Boolean {
        var status = true

        insert.setNullableString(table.LOCATION_MRID.queryIndex, asset.location?.mRID)
        asset.organisationRoles.forEach { status = status and saveAssociation(it, asset) }

        return status and saveIdentifiedObject(table, insert, asset, description)
    }

    @Throws(SQLException::class)
    private fun saveAssetContainer(table: TableAssetContainers, insert: PreparedStatement, assetContainer: AssetContainer, description: String): Boolean {
        return saveAsset(table, insert, assetContainer, description)
    }

    @Throws(SQLException::class)
    private fun saveAssetInfo(table: TableAssetInfo, insert: PreparedStatement, assetInfo: AssetInfo, description: String): Boolean {
        return saveIdentifiedObject(table, insert, assetInfo, description)
    }

    @Throws(SQLException::class)
    private fun saveAssetOrganisationRole(
        table: TableAssetOrganisationRoles,
        insert: PreparedStatement,
        assetOrganisationRole: AssetOrganisationRole,
        description: String
    ): Boolean {
        return saveOrganisationRole(table, insert, assetOrganisationRole, description)
    }

    /**
     * Save the [AssetOwner] fields to [TableAssetOwners].
     *
     * @param assetOwner The [AssetOwner] instance to write to the database.
     *
     * @return true if the [AssetOwner] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(assetOwner: AssetOwner): Boolean {
        val table = databaseTables.getTable<TableAssetOwners>()
        val insert = databaseTables.getInsert<TableAssetOwners>()

        return saveAssetOrganisationRole(table, insert, assetOwner, "asset owner")
    }

    @Throws(SQLException::class)
    private fun saveStructure(table: TableStructures, insert: PreparedStatement, structure: Structure, description: String): Boolean {
        return saveAssetContainer(table, insert, structure, description)
    }

    /**
     * Save the [Pole] fields to [TablePoles].
     *
     * @param pole The [Pole] instance to write to the database.
     *
     * @return true if the [Pole] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(pole: Pole): Boolean {
        val table = databaseTables.getTable<TablePoles>()
        val insert = databaseTables.getInsert<TablePoles>()

        insert.setString(table.CLASSIFICATION.queryIndex, pole.classification)

        return saveStructure(table, insert, pole, "pole")
    }

    /**
     * Save the [Streetlight] fields to [TableStreetlights].
     *
     * @param streetlight The [Streetlight] instance to write to the database.
     *
     * @return true if the [Streetlight] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(streetlight: Streetlight): Boolean {
        val table = databaseTables.getTable<TableStreetlights>()
        val insert = databaseTables.getInsert<TableStreetlights>()

        insert.setNullableString(table.POLE_MRID.queryIndex, streetlight.pole?.mRID)
        insert.setNullableInt(table.LIGHT_RATING.queryIndex, streetlight.lightRating)
        insert.setString(table.LAMP_KIND.queryIndex, streetlight.lampKind.name)
        return saveAsset(table, insert, streetlight, "streetlight")
    }

    // ###################
    // # IEC61968 Common #
    // ###################

    private fun insertStreetDetail(table: TableStreetAddresses, insert: PreparedStatement, streetDetail: StreetDetail?) {
        insert.setNullableString(table.BUILDING_NAME.queryIndex, streetDetail?.buildingName)
        insert.setNullableString(table.FLOOR_IDENTIFICATION.queryIndex, streetDetail?.floorIdentification)
        insert.setNullableString(table.STREET_NAME.queryIndex, streetDetail?.name)
        insert.setNullableString(table.NUMBER.queryIndex, streetDetail?.number)
        insert.setNullableString(table.SUITE_NUMBER.queryIndex, streetDetail?.suiteNumber)
        insert.setNullableString(table.TYPE.queryIndex, streetDetail?.type)
        insert.setNullableString(table.DISPLAY_ADDRESS.queryIndex, streetDetail?.displayAddress)
    }

    private fun insertTownDetail(table: TableTownDetails, insert: PreparedStatement, townDetail: TownDetail?) {
        insert.setNullableString(table.TOWN_NAME.queryIndex, townDetail?.name)
        insert.setNullableString(table.STATE_OR_PROVINCE.queryIndex, townDetail?.stateOrProvince)
    }

    /**
     * Save the [Location] fields to [TableLocations].
     *
     * @param location The [Location] instance to write to the database.
     *
     * @return true if the [Location] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(location: Location): Boolean {
        val table = databaseTables.getTable<TableLocations>()
        val insert = databaseTables.getInsert<TableLocations>()

        var status = saveLocationStreetAddress(location, TableLocationStreetAddressField.mainAddress, location.mainAddress, "location main address")
        location.points.forEachIndexed { sequenceNumber, point -> status = status and savePositionPoint(location, sequenceNumber, point) }

        return status and saveIdentifiedObject(table, insert, location, "location")
    }

    @Throws(SQLException::class)
    private fun saveLocationStreetAddress(
        location: Location,
        field: TableLocationStreetAddressField,
        streetAddress: StreetAddress?,
        description: String
    ): Boolean {
        if (streetAddress == null)
            return true

        val table = databaseTables.getTable<TableLocationStreetAddresses>()
        val insert = databaseTables.getInsert<TableLocationStreetAddresses>()

        insert.setNullableString(table.LOCATION_MRID.queryIndex, location.mRID)
        insert.setNullableString(table.ADDRESS_FIELD.queryIndex, field.name)

        return saveStreetAddress(
            table,
            insert,
            streetAddress,
            description
        )
    }

    @Throws(SQLException::class)
    private fun savePositionPoint(location: Location, sequenceNumber: Int, positionPoint: PositionPoint): Boolean {
        val table = databaseTables.getTable<TablePositionPoints>()
        val insert = databaseTables.getInsert<TablePositionPoints>()

        insert.setNullableString(table.LOCATION_MRID.queryIndex, location.mRID)
        insert.setNullableInt(table.SEQUENCE_NUMBER.queryIndex, sequenceNumber)
        insert.setNullableDouble(table.X_POSITION.queryIndex, positionPoint.xPosition)
        insert.setNullableDouble(table.Y_POSITION.queryIndex, positionPoint.yPosition)

        return insert.tryExecuteSingleUpdate("position point")
    }

    @Throws(SQLException::class)
    private fun saveStreetAddress(
        table: TableStreetAddresses,
        insert: PreparedStatement,
        streetAddress: StreetAddress,
        description: String
    ): Boolean {
        insert.setString(table.POSTAL_CODE.queryIndex, streetAddress.postalCode)
        insert.setString(table.PO_BOX.queryIndex, streetAddress.poBox)

        insertTownDetail(table, insert, streetAddress.townDetail)
        insertStreetDetail(table, insert, streetAddress.streetDetail)

        return insert.tryExecuteSingleUpdate(description)
    }

    // #####################################
    // # IEC61968 infIEC61968 InfAssetInfo #
    // #####################################

    /**
     * Save the [RelayInfo] fields to [TableRelayInfo].
     *
     * @param relayInfo The [RelayInfo] instance to write to the database.
     *
     * @return true if the [RelayInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(relayInfo: RelayInfo): Boolean {
        val table = databaseTables.getTable<TableRelayInfo>()
        val insert = databaseTables.getInsert<TableRelayInfo>()

        val recloseDelayTable = databaseTables.getTable<TableRecloseDelays>()
        val recloseDelayInsert = databaseTables.getInsert<TableRecloseDelays>()
        relayInfo.recloseDelays.forEachIndexed { idx, delay ->
            recloseDelayInsert.setString(recloseDelayTable.RELAY_INFO_MRID.queryIndex, relayInfo.mRID)
            recloseDelayInsert.setInt(recloseDelayTable.SEQUENCE_NUMBER.queryIndex, idx)
            recloseDelayInsert.setDouble(recloseDelayTable.RECLOSE_DELAY.queryIndex, delay)
            recloseDelayInsert.tryExecuteSingleUpdate("reclose delay")
        }

        insert.setNullableString(table.CURVE_SETTING.queryIndex, relayInfo.curveSetting)
        insert.setNullableBoolean(table.RECLOSE_FAST.queryIndex, relayInfo.recloseFast)

        return saveAssetInfo(table, insert, relayInfo, "relay info")
    }

    /**
     * Save the [CurrentTransformerInfo] fields to [TableCurrentTransformerInfo].
     *
     * @param currentTransformerInfo The [CurrentTransformerInfo] instance to write to the database.
     *
     * @return true if the [CurrentTransformerInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(currentTransformerInfo: CurrentTransformerInfo): Boolean {
        val table = databaseTables.getTable<TableCurrentTransformerInfo>()
        val insert = databaseTables.getInsert<TableCurrentTransformerInfo>()

        insert.setNullableString(table.ACCURACY_CLASS.queryIndex, currentTransformerInfo.accuracyClass)
        insert.setNullableDouble(table.ACCURACY_LIMIT.queryIndex, currentTransformerInfo.accuracyLimit)
        insert.setNullableInt(table.CORE_COUNT.queryIndex, currentTransformerInfo.coreCount)
        insert.setNullableString(table.CT_CLASS.queryIndex, currentTransformerInfo.ctClass)
        insert.setNullableInt(table.KNEE_POINT_VOLTAGE.queryIndex, currentTransformerInfo.kneePointVoltage)
        insert.setNullableRatio(table.MAX_RATIO_NUMERATOR.queryIndex, table.MAX_RATIO_DENOMINATOR.queryIndex, currentTransformerInfo.maxRatio)
        insert.setNullableRatio(table.NOMINAL_RATIO_NUMERATOR.queryIndex, table.NOMINAL_RATIO_DENOMINATOR.queryIndex, currentTransformerInfo.nominalRatio)
        insert.setNullableDouble(table.PRIMARY_RATIO.queryIndex, currentTransformerInfo.primaryRatio)
        insert.setNullableInt(table.RATED_CURRENT.queryIndex, currentTransformerInfo.ratedCurrent)
        insert.setNullableInt(table.SECONDARY_FLS_RATING.queryIndex, currentTransformerInfo.secondaryFlsRating)
        insert.setNullableDouble(table.SECONDARY_RATIO.queryIndex, currentTransformerInfo.secondaryRatio)
        insert.setNullableString(table.USAGE.queryIndex, currentTransformerInfo.usage)

        return saveAssetInfo(table, insert, currentTransformerInfo, "current transformer info")
    }

    /**
     * Save the [PotentialTransformerInfo] fields to [TablePotentialTransformerInfo].
     *
     * @param potentialTransformerInfo The [PotentialTransformerInfo] instance to write to the database.
     *
     * @return true if the [PotentialTransformerInfo] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(potentialTransformerInfo: PotentialTransformerInfo): Boolean {
        val table = databaseTables.getTable<TablePotentialTransformerInfo>()
        val insert = databaseTables.getInsert<TablePotentialTransformerInfo>()

        insert.setNullableString(table.ACCURACY_CLASS.queryIndex, potentialTransformerInfo.accuracyClass)
        insert.setNullableRatio(table.NOMINAL_RATIO_NUMERATOR.queryIndex, table.NOMINAL_RATIO_DENOMINATOR.queryIndex, potentialTransformerInfo.nominalRatio)
        insert.setNullableDouble(table.PRIMARY_RATIO.queryIndex, potentialTransformerInfo.primaryRatio)
        insert.setNullableString(table.PT_CLASS.queryIndex, potentialTransformerInfo.ptClass)
        insert.setNullableInt(table.RATED_VOLTAGE.queryIndex, potentialTransformerInfo.ratedVoltage)
        insert.setNullableDouble(table.SECONDARY_RATIO.queryIndex, potentialTransformerInfo.secondaryRatio)

        return saveAssetInfo(table, insert, potentialTransformerInfo, "potential transformer info")
    }

    // #####################
    // # IEC61968 Metering #
    // #####################

    @Throws(SQLException::class)
    private fun saveEndDevice(table: TableEndDevices, insert: PreparedStatement, endDevice: EndDevice, description: String): Boolean {
        insert.setNullableString(table.CUSTOMER_MRID.queryIndex, endDevice.customerMRID)
        insert.setNullableString(table.SERVICE_LOCATION_MRID.queryIndex, endDevice.serviceLocation?.mRID)

        var status = true
        endDevice.usagePoints.forEach { status = status and saveAssociation(it, endDevice) }

        return status and saveAssetContainer(table, insert, endDevice, description)
    }

    /**
     * Save the [Meter] fields to [TableMeters].
     *
     * @param meter The [Meter] instance to write to the database.
     *
     * @return true if the [Meter] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(meter: Meter): Boolean {
        val table = databaseTables.getTable<TableMeters>()
        val insert = databaseTables.getInsert<TableMeters>()

        return saveEndDevice(table, insert, meter, "meter")
    }

    /**
     * Save the [UsagePoint] fields to [TableUsagePoints].
     *
     * @param usagePoint The [UsagePoint] instance to write to the database.
     *
     * @return true if the [UsagePoint] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(usagePoint: UsagePoint): Boolean {
        val table = databaseTables.getTable<TableUsagePoints>()
        val insert = databaseTables.getInsert<TableUsagePoints>()

        insert.setNullableString(table.LOCATION_MRID.queryIndex, usagePoint.usagePointLocation?.mRID)
        insert.setBoolean(table.IS_VIRTUAL.queryIndex, usagePoint.isVirtual)
        insert.setNullableString(table.CONNECTION_CATEGORY.queryIndex, usagePoint.connectionCategory)
        insert.setNullableInt(table.RATED_POWER.queryIndex, usagePoint.ratedPower)
        insert.setNullableInt(table.APPROVED_INVERTER_CAPACITY.queryIndex, usagePoint.approvedInverterCapacity)

        var status = true
        usagePoint.equipment.forEach { status = status and saveAssociation(it, usagePoint) }

        return status and saveIdentifiedObject(table, insert, usagePoint, "usage point")
    }

    // #######################
    // # IEC61968 Operations #
    // #######################

    /**
     * Save the [OperationalRestriction] fields to [TableOperationalRestrictions].
     *
     * @param operationalRestriction The [OperationalRestriction] instance to write to the database.
     *
     * @return true if the [OperationalRestriction] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(operationalRestriction: OperationalRestriction): Boolean {
        val table = databaseTables.getTable<TableOperationalRestrictions>()
        val insert = databaseTables.getInsert<TableOperationalRestrictions>()

        var status = true
        operationalRestriction.equipment.forEach { status = status and saveAssociation(it, operationalRestriction) }

        return status and saveDocument(table, insert, operationalRestriction, "operational restriction")
    }

    // #####################################
    // # IEC61970 Base Auxiliary Equipment #
    // #####################################

    @Throws(SQLException::class)
    private fun saveAuxiliaryEquipment(
        table: TableAuxiliaryEquipment,
        insert: PreparedStatement,
        auxiliaryEquipment: AuxiliaryEquipment,
        description: String
    ): Boolean {
        insert.setNullableString(table.TERMINAL_MRID.queryIndex, auxiliaryEquipment.terminal?.mRID)

        return saveEquipment(table, insert, auxiliaryEquipment, description)
    }

    /**
     * Save the [CurrentTransformer] fields to [TableCurrentTransformers].
     *
     * @param currentTransformer The [CurrentTransformer] instance to write to the database.
     *
     * @return true if the [CurrentTransformer] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(currentTransformer: CurrentTransformer): Boolean {
        val table = databaseTables.getTable<TableCurrentTransformers>()
        val insert = databaseTables.getInsert<TableCurrentTransformers>()

        insert.setNullableString(table.CURRENT_TRANSFORMER_INFO_MRID.queryIndex, currentTransformer.assetInfo?.mRID)
        insert.setNullableInt(table.CORE_BURDEN.queryIndex, currentTransformer.coreBurden)

        return saveSensor(table, insert, currentTransformer, "current transformer")
    }

    /**
     * Save the [FaultIndicator] fields to [TableFaultIndicators].
     *
     * @param faultIndicator The [FaultIndicator] instance to write to the database.
     *
     * @return true if the [FaultIndicator] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(faultIndicator: FaultIndicator): Boolean {
        val table = databaseTables.getTable<TableFaultIndicators>()
        val insert = databaseTables.getInsert<TableFaultIndicators>()

        return saveAuxiliaryEquipment(table, insert, faultIndicator, "fault indicator")
    }

    /**
     * Save the [PotentialTransformer] fields to [TablePotentialTransformers].
     *
     * @param potentialTransformer The [PotentialTransformer] instance to write to the database.
     *
     * @return true if the [PotentialTransformer] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(potentialTransformer: PotentialTransformer): Boolean {
        val table = databaseTables.getTable<TablePotentialTransformers>()
        val insert = databaseTables.getInsert<TablePotentialTransformers>()

        insert.setNullableString(table.POTENTIAL_TRANSFORMER_INFO_MRID.queryIndex, potentialTransformer.assetInfo?.mRID)
        insert.setString(table.TYPE.queryIndex, potentialTransformer.type.name)

        return saveSensor(table, insert, potentialTransformer, "potential transformer")
    }

    @Throws(SQLException::class)
    private fun saveSensor(table: TableSensors, insert: PreparedStatement, sensor: Sensor, description: String): Boolean {
        return saveAuxiliaryEquipment(table, insert, sensor, description)
    }

    // ######################
    // # IEC61970 Base Core #
    // ######################

    @Throws(SQLException::class)
    private fun saveAcDcTerminal(table: TableAcDcTerminals, insert: PreparedStatement, acDcTerminal: AcDcTerminal, description: String): Boolean {
        return saveIdentifiedObject(table, insert, acDcTerminal, description)
    }

    /**
     * Save the [BaseVoltage] fields to [TableBaseVoltages].
     *
     * @param baseVoltage The [BaseVoltage] instance to write to the database.
     *
     * @return true if the [BaseVoltage] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(baseVoltage: BaseVoltage): Boolean {
        val table = databaseTables.getTable<TableBaseVoltages>()
        val insert = databaseTables.getInsert<TableBaseVoltages>()

        insert.setInt(table.NOMINAL_VOLTAGE.queryIndex, baseVoltage.nominalVoltage)

        return saveIdentifiedObject(table, insert, baseVoltage, "base voltage")
    }

    @Throws(SQLException::class)
    private fun saveConductingEquipment(
        table: TableConductingEquipment,
        insert: PreparedStatement,
        conductingEquipment: ConductingEquipment,
        description: String
    ): Boolean {
        insert.setNullableString(table.BASE_VOLTAGE_MRID.queryIndex, conductingEquipment.baseVoltage?.mRID)

        return saveEquipment(table, insert, conductingEquipment, description)
    }

    /**
     * Save the [ConnectivityNode] fields to [TableConnectivityNodes].
     *
     * @param connectivityNode The [ConnectivityNode] instance to write to the database.
     *
     * @return true if the [ConnectivityNode] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(connectivityNode: ConnectivityNode): Boolean {
        val table = databaseTables.getTable<TableConnectivityNodes>()
        val insert = databaseTables.getInsert<TableConnectivityNodes>()

        return saveIdentifiedObject(table, insert, connectivityNode, "connectivity node")
    }

    @Throws(SQLException::class)
    private fun saveConnectivityNodeContainer(
        table: TableConnectivityNodeContainers,
        insert: PreparedStatement,
        connectivityNodeContainer: ConnectivityNodeContainer,
        description: String
    ): Boolean {
        return savePowerSystemResource(table, insert, connectivityNodeContainer, description)
    }

    @Throws(SQLException::class)
    private fun saveEquipment(table: TableEquipment, insert: PreparedStatement, equipment: Equipment, description: String): Boolean {
        insert.setBoolean(table.NORMALLY_IN_SERVICE.queryIndex, equipment.normallyInService)
        insert.setBoolean(table.IN_SERVICE.queryIndex, equipment.inService)
        insert.setInstant(table.COMMISSIONED_DATE.queryIndex, equipment.commissionedDate)

        var status = true
        equipment.containers.forEach {
            if (it !is Feeder)
                status = status and saveAssociation(equipment, it)
        }

        return status and savePowerSystemResource(table, insert, equipment, description)
    }

    @Throws(SQLException::class)
    private fun saveEquipmentContainer(
        table: TableEquipmentContainers,
        insert: PreparedStatement,
        equipmentContainer: EquipmentContainer,
        description: String
    ): Boolean {
        return saveConnectivityNodeContainer(table, insert, equipmentContainer, description)
    }

    /**
     * Save the [Feeder] fields to [TableFeeders].
     *
     * @param feeder The [Feeder] instance to write to the database.
     *
     * @return true if the [Feeder] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(feeder: Feeder): Boolean {
        val table = databaseTables.getTable<TableFeeders>()
        val insert = databaseTables.getInsert<TableFeeders>()

        insert.setNullableString(table.NORMAL_HEAD_TERMINAL_MRID.queryIndex, feeder.normalHeadTerminal?.mRID)
        insert.setNullableString(
            table.NORMAL_ENERGIZING_SUBSTATION_MRID.queryIndex,
            feeder.normalEnergizingSubstation?.mRID
        )

        return saveEquipmentContainer(table, insert, feeder, "feeder")
    }

    /**
     * Save the [GeographicalRegion] fields to [TableGeographicalRegions].
     *
     * @param geographicalRegion The [GeographicalRegion] instance to write to the database.
     *
     * @return true if the [GeographicalRegion] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(geographicalRegion: GeographicalRegion): Boolean {
        val table = databaseTables.getTable<TableGeographicalRegions>()
        val insert = databaseTables.getInsert<TableGeographicalRegions>()

        return saveIdentifiedObject(table, insert, geographicalRegion, "geographical region")
    }

    @Throws(SQLException::class)
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

    /**
     * Save the [Site] fields to [TableSites].
     *
     * @param site The [Site] instance to write to the database.
     *
     * @return true if the [Site] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(site: Site): Boolean {
        val table = databaseTables.getTable<TableSites>()
        val insert = databaseTables.getInsert<TableSites>()

        return saveEquipmentContainer(table, insert, site, "site")
    }

    /**
     * Save the [SubGeographicalRegion] fields to [TableSubGeographicalRegions].
     *
     * @param subGeographicalRegion The [SubGeographicalRegion] instance to write to the database.
     *
     * @return true if the [SubGeographicalRegion] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(subGeographicalRegion: SubGeographicalRegion): Boolean {
        val table = databaseTables.getTable<TableSubGeographicalRegions>()
        val insert = databaseTables.getInsert<TableSubGeographicalRegions>()

        insert.setNullableString(
            table.GEOGRAPHICAL_REGION_MRID.queryIndex,
            subGeographicalRegion.geographicalRegion?.mRID
        )

        return saveIdentifiedObject(table, insert, subGeographicalRegion, "sub-geographical region")
    }

    /**
     * Save the [Substation] fields to [TableSubstations].
     *
     * @param substation The [Substation] instance to write to the database.
     *
     * @return true if the [Substation] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(substation: Substation): Boolean {
        val table = databaseTables.getTable<TableSubstations>()
        val insert = databaseTables.getInsert<TableSubstations>()

        insert.setNullableString(table.SUB_GEOGRAPHICAL_REGION_MRID.queryIndex, substation.subGeographicalRegion?.mRID)

        return saveEquipmentContainer(table, insert, substation, "substation")
    }

    /**
     * Save the [Terminal] fields to [TableTerminals].
     *
     * @param terminal The [Terminal] instance to write to the database.
     *
     * @return true if the [Terminal] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(terminal: Terminal): Boolean {
        val table = databaseTables.getTable<TableTerminals>()
        val insert = databaseTables.getInsert<TableTerminals>()

        insert.setNullableString(table.CONDUCTING_EQUIPMENT_MRID.queryIndex, terminal.conductingEquipment?.mRID)
        insert.setInt(table.SEQUENCE_NUMBER.queryIndex, terminal.sequenceNumber)
        insert.setNullableString(table.CONNECTIVITY_NODE_MRID.queryIndex, terminal.connectivityNodeId)
        insert.setNullableString(table.PHASES.queryIndex, terminal.phases.name)

        return saveAcDcTerminal(table, insert, terminal, "terminal")
    }

    // #############################
    // # IEC61970 Base Equivalents #
    // #############################

    /**
     * Save the [EquivalentBranch] fields to [TableEquivalentBranches].
     *
     * @param equivalentBranch The [EquivalentBranch] instance to write to the database.
     *
     * @return true if the [EquivalentBranch] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(equivalentBranch: EquivalentBranch): Boolean {
        val table = databaseTables.getTable<TableEquivalentBranches>()
        val insert = databaseTables.getInsert<TableEquivalentBranches>()

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

    @Throws(SQLException::class)
    private fun saveEquivalentEquipment(
        table: TableEquivalentEquipment,
        insert: PreparedStatement,
        equivalentEquipment: EquivalentEquipment,
        description: String
    ): Boolean =
        saveConductingEquipment(table, insert, equivalentEquipment, description)

    // ######################
    // # IEC61970 Base Meas #
    // ######################

    @Throws(SQLException::class)
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

    /**
     * Save the [Analog] fields to [TableAnalogs].
     *
     * @param analog The [Analog] instance to write to the database.
     *
     * @return true if the [Analog] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(analog: Analog): Boolean {
        val table = databaseTables.getTable<TableAnalogs>()
        val insert = databaseTables.getInsert<TableAnalogs>()

        insert.setBoolean(table.POSITIVE_FLOW_IN.queryIndex, analog.positiveFlowIn)

        return saveMeasurement(table, insert, analog, "analog")
    }

    /**
     * Save the [Accumulator] fields to [TableAccumulators].
     *
     * @param accumulator The [Accumulator] instance to write to the database.
     *
     * @return true if the [Accumulator] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(accumulator: Accumulator): Boolean {
        val table = databaseTables.getTable<TableAccumulators>()
        val insert = databaseTables.getInsert<TableAccumulators>()

        return saveMeasurement(table, insert, accumulator, "accumulator")
    }

    /**
     * Save the [Discrete] fields to [TableDiscretes].
     *
     * @param discrete The [Discrete] instance to write to the database.
     *
     * @return true if the [Discrete] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(discrete: Discrete): Boolean {
        val table = databaseTables.getTable<TableDiscretes>()
        val insert = databaseTables.getInsert<TableDiscretes>()

        return saveMeasurement(table, insert, discrete, "discrete")
    }

    /**
     * Save the [Control] fields to [TableControls].
     *
     * @param control The [Control] instance to write to the database.
     *
     * @return true if the [Control] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(control: Control): Boolean {
        val table = databaseTables.getTable<TableControls>()
        val insert = databaseTables.getInsert<TableControls>()

        insert.setNullableString(table.POWER_SYSTEM_RESOURCE_MRID.queryIndex, control.powerSystemResourceMRID)

        return saveIoPoint(table, insert, control, "control")
    }

    @Throws(SQLException::class)
    private fun saveIoPoint(table: TableIoPoints, insert: PreparedStatement, ioPoint: IoPoint, description: String): Boolean {
        return saveIdentifiedObject(table, insert, ioPoint, description)
    }

    // ############################
    // # IEC61970 Base Protection #
    // ############################

    /**
     * Save the [CurrentRelay] fields to [TableCurrentRelays].
     *
     * @param currentRelay The [CurrentRelay] instance to write to the database.
     *
     * @return true if the [CurrentRelay] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(currentRelay: CurrentRelay): Boolean {
        val table = databaseTables.getTable<TableCurrentRelays>()
        val insert = databaseTables.getInsert<TableCurrentRelays>()

        insert.setNullableDouble(table.CURRENT_LIMIT_1.queryIndex, currentRelay.currentLimit1)
        insert.setNullableBoolean(table.INVERSE_TIME_FLAG.queryIndex, currentRelay.inverseTimeFlag)
        insert.setNullableDouble(table.TIME_DELAY_1.queryIndex, currentRelay.timeDelay1)

        return saveProtectionRelayFunction(table, insert, currentRelay, "current relay")
    }

    /**
     * Save the [DistanceRelay] fields to [TableDistanceRelays].
     *
     * @param distanceRelay The [DistanceRelay] instance to write to the database.
     *
     * @return true if the [DistanceRelay] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(distanceRelay: DistanceRelay): Boolean {
        val table = databaseTables.getTable<TableDistanceRelays>()
        val insert = databaseTables.getInsert<TableDistanceRelays>()

        insert.setNullableDouble(table.BACKWARD_BLIND.queryIndex, distanceRelay.backwardBlind)
        insert.setNullableDouble(table.BACKWARD_REACH.queryIndex, distanceRelay.backwardReach)
        insert.setNullableDouble(table.BACKWARD_REACTANCE.queryIndex, distanceRelay.backwardReactance)
        insert.setNullableDouble(table.FORWARD_BLIND.queryIndex, distanceRelay.forwardBlind)
        insert.setNullableDouble(table.FORWARD_REACH.queryIndex, distanceRelay.forwardReach)
        insert.setNullableDouble(table.FORWARD_REACTANCE.queryIndex, distanceRelay.forwardReactance)
        insert.setNullableDouble(table.OPERATION_PHASE_ANGLE1.queryIndex, distanceRelay.operationPhaseAngle1)
        insert.setNullableDouble(table.OPERATION_PHASE_ANGLE2.queryIndex, distanceRelay.operationPhaseAngle2)
        insert.setNullableDouble(table.OPERATION_PHASE_ANGLE3.queryIndex, distanceRelay.operationPhaseAngle3)

        return saveProtectionRelayFunction(table, insert, distanceRelay, "distance relay")
    }

    @Throws(SQLException::class)
    private fun saveProtectionRelayFunction(
        table: TableProtectionRelayFunctions,
        insert: PreparedStatement,
        protectionRelayFunction: ProtectionRelayFunction,
        description: String
    ): Boolean {
        insert.setNullableString(table.MODEL.queryIndex, protectionRelayFunction.model)
        insert.setNullableBoolean(table.RECLOSING.queryIndex, protectionRelayFunction.reclosing)
        insert.setNullableDouble(table.RELAY_DELAY_TIME.queryIndex, protectionRelayFunction.relayDelayTime)
        insert.setString(table.PROTECTION_KIND.queryIndex, protectionRelayFunction.protectionKind.name)
        insert.setNullableBoolean(table.DIRECTABLE.queryIndex, protectionRelayFunction.directable)
        insert.setString(table.POWER_DIRECTION.queryIndex, protectionRelayFunction.powerDirection.name)
        insert.setNullableString(table.RELAY_INFO_MRID.queryIndex, protectionRelayFunction.assetInfo?.mRID)

        var status = true
        protectionRelayFunction.protectedSwitches.forEach { status = status and saveAssociation(protectionRelayFunction, it) }
        protectionRelayFunction.sensors.forEach { status = status and saveAssociation(protectionRelayFunction, it) }
        protectionRelayFunction.thresholds.forEachIndexed { sequenceNumber, threshold ->
            status = status and saveProtectionRelayFunctionThreshold(protectionRelayFunction, sequenceNumber, threshold)
        }
        protectionRelayFunction.timeLimits.forEachIndexed { sequenceNumber, timeLimit ->
            status = status and saveProtectionRelayFunctionTimeLimit(protectionRelayFunction, sequenceNumber, timeLimit)
        }

        return status and savePowerSystemResource(table, insert, protectionRelayFunction, description)
    }

    @Throws(SQLException::class)
    private fun saveProtectionRelayFunctionThreshold(protectionRelayFunction: ProtectionRelayFunction, sequenceNumber: Int, threshold: RelaySetting): Boolean {
        val table = databaseTables.getTable<TableProtectionRelayFunctionThresholds>()
        val insert = databaseTables.getInsert<TableProtectionRelayFunctionThresholds>()

        insert.setString(table.PROTECTION_RELAY_FUNCTION_MRID.queryIndex, protectionRelayFunction.mRID)
        insert.setInt(table.SEQUENCE_NUMBER.queryIndex, sequenceNumber)
        insert.setString(table.UNIT_SYMBOL.queryIndex, threshold.unitSymbol.name)
        insert.setDouble(table.VALUE.queryIndex, threshold.value)
        insert.setNullableString(table.NAME.queryIndex, threshold.name)

        return insert.tryExecuteSingleUpdate("protection relay function threshold")
    }

    @Throws(SQLException::class)
    private fun saveProtectionRelayFunctionTimeLimit(protectionRelayFunction: ProtectionRelayFunction, sequenceNumber: Int, timeLimit: Double): Boolean {
        val table = databaseTables.getTable<TableProtectionRelayFunctionTimeLimits>()
        val insert = databaseTables.getInsert<TableProtectionRelayFunctionTimeLimits>()

        insert.setString(table.PROTECTION_RELAY_FUNCTION_MRID.queryIndex, protectionRelayFunction.mRID)
        insert.setInt(table.SEQUENCE_NUMBER.queryIndex, sequenceNumber)
        insert.setDouble(table.TIME_LIMIT.queryIndex, timeLimit)

        return insert.tryExecuteSingleUpdate("protection relay function time limit")
    }

    /**
     * Save the [ProtectionRelayScheme] fields to [TableProtectionRelaySchemes].
     *
     * @param protectionRelayScheme The [ProtectionRelayScheme] instance to write to the database.
     *
     * @return true if the [ProtectionRelayScheme] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(protectionRelayScheme: ProtectionRelayScheme): Boolean {
        val table = databaseTables.getTable<TableProtectionRelaySchemes>()
        val insert = databaseTables.getInsert<TableProtectionRelaySchemes>()

        insert.setNullableString(table.SYSTEM_MRID.queryIndex, protectionRelayScheme.system?.mRID)

        var status = true
        protectionRelayScheme.functions.forEach { status = status and saveAssociation(protectionRelayScheme, it) }

        return status and saveIdentifiedObject(table, insert, protectionRelayScheme, "protection relay scheme")
    }

    /**
     * Save the [ProtectionRelaySystem] fields to [TableProtectionRelaySystems].
     *
     * @param protectionRelaySystem The [ProtectionRelaySystem] instance to write to the database.
     *
     * @return true if the [ProtectionRelaySystem] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(protectionRelaySystem: ProtectionRelaySystem): Boolean {
        val table = databaseTables.getTable<TableProtectionRelaySystems>()
        val insert = databaseTables.getInsert<TableProtectionRelaySystems>()

        insert.setString(table.PROTECTION_KIND.queryIndex, protectionRelaySystem.protectionKind.name)

        return saveEquipment(table, insert, protectionRelaySystem, "protection relay system")
    }

    /**
     * Save the [VoltageRelay] fields to [TableVoltageRelays].
     *
     * @param voltageRelay The [VoltageRelay] instance to write to the database.
     *
     * @return true if the [VoltageRelay] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(voltageRelay: VoltageRelay): Boolean {
        val table = databaseTables.getTable<TableVoltageRelays>()
        val insert = databaseTables.getInsert<TableVoltageRelays>()

        return saveProtectionRelayFunction(table, insert, voltageRelay, "voltage relay")
    }

    // ############################
    // # IEC61970 Base SCADA #
    // ############################

    /**
     * Save the [RemoteControl] fields to [TableRemoteControls].
     *
     * @param remoteControl The [RemoteControl] instance to write to the database.
     *
     * @return true if the [RemoteControl] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(remoteControl: RemoteControl): Boolean {
        val table = databaseTables.getTable<TableRemoteControls>()
        val insert = databaseTables.getInsert<TableRemoteControls>()

        insert.setNullableString(table.CONTROL_MRID.queryIndex, remoteControl.control?.mRID)

        return saveRemotePoint(table, insert, remoteControl, "remote control")
    }

    @Throws(SQLException::class)
    private fun saveRemotePoint(table: TableRemotePoints, insert: PreparedStatement, remotePoint: RemotePoint, description: String): Boolean {
        return saveIdentifiedObject(table, insert, remotePoint, description)
    }

    /**
     * Save the [RemoteSource] fields to [TableRemoteSources].
     *
     * @param remoteSource The [RemoteSource] instance to write to the database.
     *
     * @return true if the [RemoteSource] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(remoteSource: RemoteSource): Boolean {
        val table = databaseTables.getTable<TableRemoteSources>()
        val insert = databaseTables.getInsert<TableRemoteSources>()

        insert.setNullableString(table.MEASUREMENT_MRID.queryIndex, remoteSource.measurement?.mRID)

        return saveRemotePoint(table, insert, remoteSource, "remote source")
    }

    // #############################################
    // # IEC61970 Base Wires Generation Production #
    // #############################################

    /**
     * Save the [BatteryUnit] fields to [TableBatteryUnits].
     *
     * @param batteryUnit The [BatteryUnit] instance to write to the database.
     *
     * @return true if the [BatteryUnit] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(batteryUnit: BatteryUnit): Boolean {
        val table = databaseTables.getTable<TableBatteryUnits>()
        val insert = databaseTables.getInsert<TableBatteryUnits>()

        insert.setString(table.BATTERY_STATE.queryIndex, batteryUnit.batteryState.name)
        insert.setNullableLong(table.RATED_E.queryIndex, batteryUnit.ratedE)
        insert.setNullableLong(table.STORED_E.queryIndex, batteryUnit.storedE)

        return savePowerElectronicsUnit(table, insert, batteryUnit, "battery unit")
    }

    /**
     * Save the [PhotoVoltaicUnit] fields to [TablePhotoVoltaicUnits].
     *
     * @param photoVoltaicUnit The [PhotoVoltaicUnit] instance to write to the database.
     *
     * @return true if the [PhotoVoltaicUnit] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(photoVoltaicUnit: PhotoVoltaicUnit): Boolean {
        val table = databaseTables.getTable<TablePhotoVoltaicUnits>()
        val insert = databaseTables.getInsert<TablePhotoVoltaicUnits>()

        return savePowerElectronicsUnit(table, insert, photoVoltaicUnit, "photo voltaic unit")
    }

    @Throws(SQLException::class)
    private fun savePowerElectronicsUnit(
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

    /**
     * Save the [PowerElectronicsWindUnit] fields to [TablePowerElectronicsWindUnits].
     *
     * @param powerElectronicsWindUnit The [PowerElectronicsWindUnit] instance to write to the database.
     *
     * @return true if the [PowerElectronicsWindUnit] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(powerElectronicsWindUnit: PowerElectronicsWindUnit): Boolean {
        val table = databaseTables.getTable<TablePowerElectronicsWindUnits>()
        val insert = databaseTables.getInsert<TablePowerElectronicsWindUnits>()

        return savePowerElectronicsUnit(table, insert, powerElectronicsWindUnit, "power electronics wind unit")
    }

    // #######################
    // # IEC61970 Base Wires #
    // #######################

    /**
     * Save the [AcLineSegment] fields to [TableAcLineSegments].
     *
     * @param acLineSegment The [AcLineSegment] instance to write to the database.
     *
     * @return true if the [AcLineSegment] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(acLineSegment: AcLineSegment): Boolean {
        val table = databaseTables.getTable<TableAcLineSegments>()
        val insert = databaseTables.getInsert<TableAcLineSegments>()

        insert.setNullableString(
            table.PER_LENGTH_SEQUENCE_IMPEDANCE_MRID.queryIndex,
            acLineSegment.perLengthSequenceImpedance?.mRID
        )

        return saveConductor(table, insert, acLineSegment, "AC line segment")
    }

    /**
     * Save the [Breaker] fields to [TableBreakers].
     *
     * @param breaker The [Breaker] instance to write to the database.
     *
     * @return true if the [Breaker] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(breaker: Breaker): Boolean {
        val table = databaseTables.getTable<TableBreakers>()
        val insert = databaseTables.getInsert<TableBreakers>()

        insert.setNullableDouble(table.IN_TRANSIT_TIME.queryIndex, breaker.inTransitTime)

        return saveProtectedSwitch(table, insert, breaker, "breaker")
    }

    /**
     * Save the [BusbarSection] fields to [TableBusbarSections].
     *
     * @param busbarSection The [BusbarSection] instance to write to the database.
     *
     * @return true if the [BusbarSection] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(busbarSection: BusbarSection): Boolean {
        val table = databaseTables.getTable<TableBusbarSections>()
        val insert = databaseTables.getInsert<TableBusbarSections>()

        return saveConnector(table, insert, busbarSection, "busbar section")
    }

    @Throws(SQLException::class)
    private fun saveConductor(table: TableConductors, insert: PreparedStatement, conductor: Conductor, description: String): Boolean {
        insert.setNullableDouble(table.LENGTH.queryIndex, conductor.length)
        insert.setNullableString(table.WIRE_INFO_MRID.queryIndex, conductor.assetInfo?.mRID)

        return saveConductingEquipment(table, insert, conductor, description)
    }

    @Throws(SQLException::class)
    private fun saveConnector(table: TableConnectors, insert: PreparedStatement, connector: Connector, description: String): Boolean {
        return saveConductingEquipment(table, insert, connector, description)
    }

    /**
     * Save the [Disconnector] fields to [TableDisconnectors].
     *
     * @param disconnector The [Disconnector] instance to write to the database.
     *
     * @return true if the [Disconnector] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(disconnector: Disconnector): Boolean {
        val table = databaseTables.getTable<TableDisconnectors>()
        val insert = databaseTables.getInsert<TableDisconnectors>()

        return saveSwitch(table, insert, disconnector, "disconnector")
    }

    @Throws(SQLException::class)
    private fun saveEnergyConnection(
        table: TableEnergyConnections,
        insert: PreparedStatement,
        energyConnection: EnergyConnection,
        description: String
    ): Boolean {
        return saveConductingEquipment(table, insert, energyConnection, description)
    }

    /**
     * Save the [EnergyConsumer] fields to [TableEnergyConsumers].
     *
     * @param energyConsumer The [EnergyConsumer] instance to write to the database.
     *
     * @return true if the [EnergyConsumer] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(energyConsumer: EnergyConsumer): Boolean {
        val table = databaseTables.getTable<TableEnergyConsumers>()
        val insert = databaseTables.getInsert<TableEnergyConsumers>()

        insert.setNullableInt(table.CUSTOMER_COUNT.queryIndex, energyConsumer.customerCount)
        insert.setBoolean(table.GROUNDED.queryIndex, energyConsumer.grounded)
        insert.setNullableDouble(table.P.queryIndex, energyConsumer.p)
        insert.setNullableDouble(table.Q.queryIndex, energyConsumer.q)
        insert.setNullableDouble(table.P_FIXED.queryIndex, energyConsumer.pFixed)
        insert.setNullableDouble(table.Q_FIXED.queryIndex, energyConsumer.qFixed)
        insert.setNullableString(table.PHASE_CONNECTION.queryIndex, energyConsumer.phaseConnection.name)

        return saveEnergyConnection(table, insert, energyConsumer, "energy consumer")
    }

    /**
     * Save the [EnergyConsumerPhase] fields to [TableEnergyConsumerPhases].
     *
     * @param energyConsumerPhase The [EnergyConsumerPhase] instance to write to the database.
     *
     * @return true if the [EnergyConsumerPhase] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(energyConsumerPhase: EnergyConsumerPhase): Boolean {
        val table = databaseTables.getTable<TableEnergyConsumerPhases>()
        val insert = databaseTables.getInsert<TableEnergyConsumerPhases>()

        insert.setNullableString(table.ENERGY_CONSUMER_MRID.queryIndex, energyConsumerPhase.energyConsumer?.mRID)
        insert.setNullableString(table.PHASE.queryIndex, energyConsumerPhase.phase.name)
        insert.setNullableDouble(table.P.queryIndex, energyConsumerPhase.p)
        insert.setNullableDouble(table.Q.queryIndex, energyConsumerPhase.q)
        insert.setNullableDouble(table.P_FIXED.queryIndex, energyConsumerPhase.pFixed)
        insert.setNullableDouble(table.Q_FIXED.queryIndex, energyConsumerPhase.qFixed)

        return savePowerSystemResource(table, insert, energyConsumerPhase, "energy consumer phase")
    }

    /**
     * Save the [EnergySource] fields to [TableEnergySources].
     *
     * @param energySource The [EnergySource] instance to write to the database.
     *
     * @return true if the [EnergySource] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(energySource: EnergySource): Boolean {
        val table = databaseTables.getTable<TableEnergySources>()
        val insert = databaseTables.getInsert<TableEnergySources>()

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
        insert.setBoolean(table.IS_EXTERNAL_GRID.queryIndex, energySource.isExternalGrid)
        insert.setNullableDouble(table.R_MIN.queryIndex, energySource.rMin)
        insert.setNullableDouble(table.RN_MIN.queryIndex, energySource.rnMin)
        insert.setNullableDouble(table.R0_MIN.queryIndex, energySource.r0Min)
        insert.setNullableDouble(table.X_MIN.queryIndex, energySource.xMin)
        insert.setNullableDouble(table.XN_MIN.queryIndex, energySource.xnMin)
        insert.setNullableDouble(table.X0_MIN.queryIndex, energySource.x0Min)
        insert.setNullableDouble(table.R_MAX.queryIndex, energySource.rMax)
        insert.setNullableDouble(table.RN_MAX.queryIndex, energySource.rnMax)
        insert.setNullableDouble(table.R0_MAX.queryIndex, energySource.r0Max)
        insert.setNullableDouble(table.X_MAX.queryIndex, energySource.xMax)
        insert.setNullableDouble(table.XN_MAX.queryIndex, energySource.xnMax)
        insert.setNullableDouble(table.X0_MAX.queryIndex, energySource.x0Max)

        return saveEnergyConnection(table, insert, energySource, "energy source")
    }

    /**
     * Save the [EnergySourcePhase] fields to [TableEnergySourcePhases].
     *
     * @param energySourcePhase The [EnergySourcePhase] instance to write to the database.
     *
     * @return true if the [EnergySourcePhase] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(energySourcePhase: EnergySourcePhase): Boolean {
        val table = databaseTables.getTable<TableEnergySourcePhases>()
        val insert = databaseTables.getInsert<TableEnergySourcePhases>()

        insert.setNullableString(table.ENERGY_SOURCE_MRID.queryIndex, energySourcePhase.energySource?.mRID)
        insert.setNullableString(table.PHASE.queryIndex, energySourcePhase.phase.name)

        return savePowerSystemResource(table, insert, energySourcePhase, "energy source phase")
    }

    /**
     * Save the [Fuse] fields to [TableFuses].
     *
     * @param fuse The [Fuse] instance to write to the database.
     *
     * @return true if the [Fuse] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(fuse: Fuse): Boolean {
        val table = databaseTables.getTable<TableFuses>()
        val insert = databaseTables.getInsert<TableFuses>()

        insert.setNullableString(table.FUNCTION_MRID.queryIndex, fuse.function?.mRID)

        return saveSwitch(table, insert, fuse, "fuse")
    }

    /**
     * Save the [Ground] fields to [TableGrounds].
     *
     * @param ground The [Ground] instance to write to the database.
     *
     * @return true if the [Ground] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(ground: Ground): Boolean {
        val table = databaseTables.getTable<TableGrounds>()
        val insert = databaseTables.getInsert<TableGrounds>()

        return saveConductingEquipment(table, insert, ground, "ground")
    }

    /**
     * Save the [GroundDisconnector] fields to [TableGroundDisconnectors].
     *
     * @param groundDisconnector The [GroundDisconnector] instance to write to the database.
     *
     * @return true if the [GroundDisconnector] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(groundDisconnector: GroundDisconnector): Boolean {
        val table = databaseTables.getTable<TableGroundDisconnectors>()
        val insert = databaseTables.getInsert<TableGroundDisconnectors>()

        return saveSwitch(table, insert, groundDisconnector, "ground disconnector")
    }

    /**
     * Save the [Jumper] fields to [TableJumpers].
     *
     * @param jumper The [Jumper] instance to write to the database.
     *
     * @return true if the [Jumper] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(jumper: Jumper): Boolean {
        val table = databaseTables.getTable<TableJumpers>()
        val insert = databaseTables.getInsert<TableJumpers>()

        return saveSwitch(table, insert, jumper, "jumper")
    }

    /**
     * Save the [Junction] fields to [TableJunctions].
     *
     * @param junction The [Junction] instance to write to the database.
     *
     * @return true if the [Junction] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(junction: Junction): Boolean {
        val table = databaseTables.getTable<TableJunctions>()
        val insert = databaseTables.getInsert<TableJunctions>()

        return saveConnector(table, insert, junction, "junction")
    }

    @Throws(SQLException::class)
    private fun saveLine(table: TableLines, insert: PreparedStatement, line: Line, description: String): Boolean {
        return saveEquipmentContainer(table, insert, line, description)
    }

    /**
     * Save the [LinearShuntCompensator] fields to [TableLinearShuntCompensators].
     *
     * @param linearShuntCompensator The [LinearShuntCompensator] instance to write to the database.
     *
     * @return true if the [LinearShuntCompensator] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(linearShuntCompensator: LinearShuntCompensator): Boolean {
        val table = databaseTables.getTable<TableLinearShuntCompensators>()
        val insert = databaseTables.getInsert<TableLinearShuntCompensators>()

        insert.setNullableDouble(table.B0_PER_SECTION.queryIndex, linearShuntCompensator.b0PerSection)
        insert.setNullableDouble(table.B_PER_SECTION.queryIndex, linearShuntCompensator.bPerSection)
        insert.setNullableDouble(table.G0_PER_SECTION.queryIndex, linearShuntCompensator.g0PerSection)
        insert.setNullableDouble(table.G_PER_SECTION.queryIndex, linearShuntCompensator.gPerSection)

        return saveShuntCompensator(table, insert, linearShuntCompensator, "linear shunt compensator")
    }

    /**
     * Save the [LoadBreakSwitch] fields to [TableLoadBreakSwitches].
     *
     * @param loadBreakSwitch The [LoadBreakSwitch] instance to write to the database.
     *
     * @return true if the [LoadBreakSwitch] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(loadBreakSwitch: LoadBreakSwitch): Boolean {
        val table = databaseTables.getTable<TableLoadBreakSwitches>()
        val insert = databaseTables.getInsert<TableLoadBreakSwitches>()

        return saveProtectedSwitch(table, insert, loadBreakSwitch, "load break switch")
    }

    @Throws(SQLException::class)
    private fun savePerLengthImpedance(
        table: TablePerLengthImpedances,
        insert: PreparedStatement,
        perLengthImpedance: PerLengthImpedance,
        description: String
    ): Boolean {
        return savePerLengthLineParameter(table, insert, perLengthImpedance, description)
    }

    @Throws(SQLException::class)
    private fun savePerLengthLineParameter(
        table: TablePerLengthLineParameters,
        insert: PreparedStatement,
        perLengthLineParameter: PerLengthLineParameter,
        description: String
    ): Boolean {
        return saveIdentifiedObject(table, insert, perLengthLineParameter, description)
    }

    /**
     * Save the [PerLengthSequenceImpedance] fields to [TablePerLengthSequenceImpedances].
     *
     * @param perLengthSequenceImpedance The [PerLengthSequenceImpedance] instance to write to the database.
     *
     * @return true if the [PerLengthSequenceImpedance] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(perLengthSequenceImpedance: PerLengthSequenceImpedance): Boolean {
        val table = databaseTables.getTable<TablePerLengthSequenceImpedances>()
        val insert = databaseTables.getInsert<TablePerLengthSequenceImpedances>()

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

    /**
     * Save the [PowerElectronicsConnection] fields to [TablePowerElectronicsConnections].
     *
     * @param powerElectronicsConnection The [PowerElectronicsConnection] instance to write to the database.
     *
     * @return true if the [PowerElectronicsConnection] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(powerElectronicsConnection: PowerElectronicsConnection): Boolean {
        val table = databaseTables.getTable<TablePowerElectronicsConnections>()
        val insert = databaseTables.getInsert<TablePowerElectronicsConnections>()

        insert.setNullableInt(table.MAX_I_FAULT.queryIndex, powerElectronicsConnection.maxIFault)
        insert.setNullableDouble(table.MAX_Q.queryIndex, powerElectronicsConnection.maxQ)
        insert.setNullableDouble(table.MIN_Q.queryIndex, powerElectronicsConnection.minQ)
        insert.setNullableDouble(table.P.queryIndex, powerElectronicsConnection.p)
        insert.setNullableDouble(table.Q.queryIndex, powerElectronicsConnection.q)
        insert.setNullableInt(table.RATED_S.queryIndex, powerElectronicsConnection.ratedS)
        insert.setNullableInt(table.RATED_U.queryIndex, powerElectronicsConnection.ratedU)
        insert.setNullableString(table.INVERTER_STANDARD.queryIndex, powerElectronicsConnection.inverterStandard)
        insert.setNullableInt(table.SUSTAIN_OP_OVERVOLT_LIMIT.queryIndex, powerElectronicsConnection.sustainOpOvervoltLimit)
        insert.setNullableFloat(table.STOP_AT_OVER_FREQ.queryIndex, powerElectronicsConnection.stopAtOverFreq)
        insert.setNullableFloat(table.STOP_AT_UNDER_FREQ.queryIndex, powerElectronicsConnection.stopAtUnderFreq)
        insert.setNullableBoolean(table.INV_VOLT_WATT_RESP_MODE.queryIndex, powerElectronicsConnection.invVoltWattRespMode)
        insert.setNullableInt(table.INV_WATT_RESP_V1.queryIndex, powerElectronicsConnection.invWattRespV1)
        insert.setNullableInt(table.INV_WATT_RESP_V2.queryIndex, powerElectronicsConnection.invWattRespV2)
        insert.setNullableInt(table.INV_WATT_RESP_V3.queryIndex, powerElectronicsConnection.invWattRespV3)
        insert.setNullableInt(table.INV_WATT_RESP_V4.queryIndex, powerElectronicsConnection.invWattRespV4)
        insert.setNullableFloat(table.INV_WATT_RESP_P_AT_V1.queryIndex, powerElectronicsConnection.invWattRespPAtV1)
        insert.setNullableFloat(table.INV_WATT_RESP_P_AT_V2.queryIndex, powerElectronicsConnection.invWattRespPAtV2)
        insert.setNullableFloat(table.INV_WATT_RESP_P_AT_V3.queryIndex, powerElectronicsConnection.invWattRespPAtV3)
        insert.setNullableFloat(table.INV_WATT_RESP_P_AT_V4.queryIndex, powerElectronicsConnection.invWattRespPAtV4)
        insert.setNullableBoolean(table.INV_VOLT_VAR_RESP_MODE.queryIndex, powerElectronicsConnection.invVoltVarRespMode)
        insert.setNullableInt(table.INV_VAR_RESP_V1.queryIndex, powerElectronicsConnection.invVarRespV1)
        insert.setNullableInt(table.INV_VAR_RESP_V2.queryIndex, powerElectronicsConnection.invVarRespV2)
        insert.setNullableInt(table.INV_VAR_RESP_V3.queryIndex, powerElectronicsConnection.invVarRespV3)
        insert.setNullableInt(table.INV_VAR_RESP_V4.queryIndex, powerElectronicsConnection.invVarRespV4)
        insert.setNullableFloat(table.INV_VAR_RESP_Q_AT_V1.queryIndex, powerElectronicsConnection.invVarRespQAtV1)
        insert.setNullableFloat(table.INV_VAR_RESP_Q_AT_V2.queryIndex, powerElectronicsConnection.invVarRespQAtV2)
        insert.setNullableFloat(table.INV_VAR_RESP_Q_AT_V3.queryIndex, powerElectronicsConnection.invVarRespQAtV3)
        insert.setNullableFloat(table.INV_VAR_RESP_Q_AT_V4.queryIndex, powerElectronicsConnection.invVarRespQAtV4)
        insert.setNullableBoolean(table.INV_REACTIVE_POWER_MODE.queryIndex, powerElectronicsConnection.invReactivePowerMode)
        insert.setNullableFloat(table.INV_FIX_REACTIVE_POWER.queryIndex, powerElectronicsConnection.invFixReactivePower)

        return saveRegulatingCondEq(table, insert, powerElectronicsConnection, "power electronics connection")
    }

    /**
     * Save the [PowerElectronicsConnectionPhase] fields to [TablePowerElectronicsConnectionPhases].
     *
     * @param powerElectronicsConnectionPhase The [PowerElectronicsConnectionPhase] instance to write to the database.
     *
     * @return true if the [PowerElectronicsConnectionPhase] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(powerElectronicsConnectionPhase: PowerElectronicsConnectionPhase): Boolean {
        val table = databaseTables.getTable<TablePowerElectronicsConnectionPhases>()
        val insert = databaseTables.getInsert<TablePowerElectronicsConnectionPhases>()

        insert.setNullableString(table.POWER_ELECTRONICS_CONNECTION_MRID.queryIndex, powerElectronicsConnectionPhase.powerElectronicsConnection?.mRID)
        insert.setNullableDouble(table.P.queryIndex, powerElectronicsConnectionPhase.p)
        insert.setString(table.PHASE.queryIndex, powerElectronicsConnectionPhase.phase.name)
        insert.setNullableDouble(table.Q.queryIndex, powerElectronicsConnectionPhase.q)

        return savePowerSystemResource(table, insert, powerElectronicsConnectionPhase, "power electronics connection phase")
    }

    /**
     * Save the [PowerTransformer] fields to [TablePowerTransformers].
     *
     * @param powerTransformer The [PowerTransformer] instance to write to the database.
     *
     * @return true if the [PowerTransformer] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(powerTransformer: PowerTransformer): Boolean {
        val table = databaseTables.getTable<TablePowerTransformers>()
        val insert = databaseTables.getInsert<TablePowerTransformers>()

        insert.setString(table.VECTOR_GROUP.queryIndex, powerTransformer.vectorGroup.name)
        insert.setNullableDouble(table.TRANSFORMER_UTILISATION.queryIndex, powerTransformer.transformerUtilisation)
        insert.setString(table.CONSTRUCTION_KIND.queryIndex, powerTransformer.constructionKind.name)
        insert.setString(table.FUNCTION.queryIndex, powerTransformer.function.name)
        insert.setNullableString(table.POWER_TRANSFORMER_INFO_MRID.queryIndex, powerTransformer.assetInfo?.mRID)

        return saveConductingEquipment(table, insert, powerTransformer, "power transformer")
    }

    /**
     * Save the [PowerTransformerEnd] fields to [TablePowerTransformerEnds].
     *
     * @param powerTransformerEnd The [PowerTransformerEnd] instance to write to the database.
     *
     * @return true if the [PowerTransformerEnd] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(powerTransformerEnd: PowerTransformerEnd): Boolean {
        val table = databaseTables.getTable<TablePowerTransformerEnds>()
        val insert = databaseTables.getInsert<TablePowerTransformerEnds>()

        insert.setNullableString(table.POWER_TRANSFORMER_MRID.queryIndex, powerTransformerEnd.powerTransformer?.mRID)
        insert.setNullableString(table.CONNECTION_KIND.queryIndex, powerTransformerEnd.connectionKind.name)
        insert.setNullableInt(table.PHASE_ANGLE_CLOCK.queryIndex, powerTransformerEnd.phaseAngleClock)
        insert.setNullableDouble(table.B.queryIndex, powerTransformerEnd.b)
        insert.setNullableDouble(table.B0.queryIndex, powerTransformerEnd.b0)
        insert.setNullableDouble(table.G.queryIndex, powerTransformerEnd.g)
        insert.setNullableDouble(table.G0.queryIndex, powerTransformerEnd.g0)
        insert.setNullableDouble(table.R.queryIndex, powerTransformerEnd.r)
        insert.setNullableDouble(table.R0.queryIndex, powerTransformerEnd.r0)
        insert.setNullableInt(table.RATED_U.queryIndex, powerTransformerEnd.ratedU)
        insert.setNullableDouble(table.X.queryIndex, powerTransformerEnd.x)
        insert.setNullableDouble(table.X0.queryIndex, powerTransformerEnd.x0)

        val ratingsTable = databaseTables.getTable<TablePowerTransformerEndRatings>()
        val ratingsInsert = databaseTables.getInsert<TablePowerTransformerEndRatings>()
        powerTransformerEnd.sRatings.forEach {
            ratingsInsert.setString(ratingsTable.POWER_TRANSFORMER_END_MRID.queryIndex, powerTransformerEnd.mRID)
            ratingsInsert.setString(ratingsTable.COOLING_TYPE.queryIndex, it.coolingType.name)
            ratingsInsert.setInt(ratingsTable.RATED_S.queryIndex, it.ratedS)
            ratingsInsert.tryExecuteSingleUpdate("transformer end ratedS")
        }

        return saveTransformerEnd(table, insert, powerTransformerEnd, "power transformer end")
    }

    @Throws(SQLException::class)
    private fun saveProtectedSwitch(table: TableProtectedSwitches, insert: PreparedStatement, protectedSwitch: ProtectedSwitch, description: String): Boolean {
        insert.setNullableInt(table.BREAKING_CAPACITY.queryIndex, protectedSwitch.breakingCapacity)

        return saveSwitch(table, insert, protectedSwitch, description)
    }

    /**
     * Save the [RatioTapChanger] fields to [TableRatioTapChangers].
     *
     * @param ratioTapChanger The [RatioTapChanger] instance to write to the database.
     *
     * @return true if the [RatioTapChanger] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(ratioTapChanger: RatioTapChanger): Boolean {
        val table = databaseTables.getTable<TableRatioTapChangers>()
        val insert = databaseTables.getInsert<TableRatioTapChangers>()

        insert.setNullableString(table.TRANSFORMER_END_MRID.queryIndex, ratioTapChanger.transformerEnd?.mRID)
        insert.setNullableDouble(table.STEP_VOLTAGE_INCREMENT.queryIndex, ratioTapChanger.stepVoltageIncrement)

        return saveTapChanger(table, insert, ratioTapChanger, "ratio tap changer")
    }

    /**
     * Save the [Recloser] fields to [TableReclosers].
     *
     * @param recloser The [Recloser] instance to write to the database.
     *
     * @return true if the [Recloser] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(recloser: Recloser): Boolean {
        val table = databaseTables.getTable<TableReclosers>()
        val insert = databaseTables.getInsert<TableReclosers>()

        return saveProtectedSwitch(table, insert, recloser, "recloser")
    }

    @Throws(SQLException::class)
    private fun saveRegulatingCondEq(
        table: TableRegulatingCondEq,
        insert: PreparedStatement,
        regulatingCondEq: RegulatingCondEq,
        description: String
    ): Boolean {
        insert.setBoolean(table.CONTROL_ENABLED.queryIndex, regulatingCondEq.controlEnabled)
        insert.setNullableString(table.REGULATING_CONTROL_MRID.queryIndex, regulatingCondEq.regulatingControl?.mRID)

        return saveEnergyConnection(table, insert, regulatingCondEq, description)
    }

    @Throws(SQLException::class)
    private fun saveRegulatingControl(
        table: TableRegulatingControls,
        insert: PreparedStatement,
        regulatingControl: RegulatingControl,
        description: String
    ): Boolean {
        insert.setNullableBoolean(table.DISCRETE.queryIndex, regulatingControl.discrete)
        insert.setString(table.MODE.queryIndex, regulatingControl.mode.name)
        insert.setString(table.MONITORED_PHASE.queryIndex, regulatingControl.monitoredPhase.name)
        insert.setNullableFloat(table.TARGET_DEADBAND.queryIndex, regulatingControl.targetDeadband)
        insert.setNullableDouble(table.TARGET_VALUE.queryIndex, regulatingControl.targetValue)
        insert.setNullableBoolean(table.ENABLED.queryIndex, regulatingControl.enabled)
        insert.setNullableDouble(table.MAX_ALLOWED_TARGET_VALUE.queryIndex, regulatingControl.maxAllowedTargetValue)
        insert.setNullableDouble(table.MIN_ALLOWED_TARGET_VALUE.queryIndex, regulatingControl.minAllowedTargetValue)
        insert.setNullableDouble(table.RATED_CURRENT.queryIndex, regulatingControl.ratedCurrent)
        insert.setNullableString(table.TERMINAL_MRID.queryIndex, regulatingControl.terminal?.mRID)

        return savePowerSystemResource(table, insert, regulatingControl, description)
    }

    /**
     * Save the [SeriesCompensator] fields to [TableSeriesCompensators].
     *
     * @param seriesCompensator The [SeriesCompensator] instance to write to the database.
     *
     * @return true if the [SeriesCompensator] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(seriesCompensator: SeriesCompensator): Boolean {
        val table = databaseTables.getTable<TableSeriesCompensators>()
        val insert = databaseTables.getInsert<TableSeriesCompensators>()

        insert.setNullableDouble(table.R.queryIndex, seriesCompensator.r)
        insert.setNullableDouble(table.R0.queryIndex, seriesCompensator.r0)
        insert.setNullableDouble(table.X.queryIndex, seriesCompensator.x)
        insert.setNullableDouble(table.X0.queryIndex, seriesCompensator.x0)
        insert.setNullableInt(table.VARISTOR_RATED_CURRENT.queryIndex, seriesCompensator.varistorRatedCurrent)
        insert.setNullableInt(table.VARISTOR_VOLTAGE_THRESHOLD.queryIndex, seriesCompensator.varistorVoltageThreshold)

        return saveConductingEquipment(table, insert, seriesCompensator, "series compensator")
    }

    @Throws(SQLException::class)
    private fun saveShuntCompensator(
        table: TableShuntCompensators,
        insert: PreparedStatement,
        shuntCompensator: ShuntCompensator,
        description: String
    ): Boolean {
        insert.setNullableString(table.SHUNT_COMPENSATOR_INFO_MRID.queryIndex, shuntCompensator.assetInfo?.mRID)
        insert.setBoolean(table.GROUNDED.queryIndex, shuntCompensator.grounded)
        insert.setNullableInt(table.NOM_U.queryIndex, shuntCompensator.nomU)
        insert.setNullableString(table.PHASE_CONNECTION.queryIndex, shuntCompensator.phaseConnection.name)
        insert.setNullableDouble(table.SECTIONS.queryIndex, shuntCompensator.sections)

        return saveRegulatingCondEq(table, insert, shuntCompensator, description)
    }

    @Throws(SQLException::class)
    private fun saveSwitch(table: TableSwitches, insert: PreparedStatement, switch: Switch, description: String): Boolean {
        insert.setInt(table.NORMAL_OPEN.queryIndex, switch.normalOpen)
        insert.setInt(table.OPEN.queryIndex, switch.open)
        insert.setNullableInt(table.RATED_CURRENT.queryIndex, switch.ratedCurrent)
        insert.setNullableString(table.SWITCH_INFO_MRID.queryIndex, switch.assetInfo?.mRID)

        return saveConductingEquipment(table, insert, switch, description)
    }

    @Throws(SQLException::class)
    private fun saveTapChanger(table: TableTapChangers, insert: PreparedStatement, tapChanger: TapChanger, description: String): Boolean {
        insert.setBoolean(table.CONTROL_ENABLED.queryIndex, tapChanger.controlEnabled)
        insert.setNullableInt(table.HIGH_STEP.queryIndex, tapChanger.highStep)
        insert.setNullableInt(table.LOW_STEP.queryIndex, tapChanger.lowStep)
        insert.setNullableInt(table.NEUTRAL_STEP.queryIndex, tapChanger.neutralStep)
        insert.setNullableInt(table.NEUTRAL_U.queryIndex, tapChanger.neutralU)
        insert.setNullableInt(table.NORMAL_STEP.queryIndex, tapChanger.normalStep)
        insert.setNullableDouble(table.STEP.queryIndex, tapChanger.step)
        insert.setNullableString(table.TAP_CHANGER_CONTROL_MRID.queryIndex, tapChanger.tapChangerControl?.mRID)

        return savePowerSystemResource(table, insert, tapChanger, description)
    }

    /**
     * Save the [TapChangerControl] fields to [TableTapChangerControls].
     *
     * @param tapChangerControl The [TapChangerControl] instance to write to the database.
     *
     * @return true if the [TapChangerControl] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(tapChangerControl: TapChangerControl): Boolean {
        val table = databaseTables.getTable<TableTapChangerControls>()
        val insert = databaseTables.getInsert<TableTapChangerControls>()

        insert.setNullableInt(table.LIMIT_VOLTAGE.queryIndex, tapChangerControl.limitVoltage)
        insert.setNullableBoolean(table.LINE_DROP_COMPENSATION.queryIndex, tapChangerControl.lineDropCompensation)
        insert.setNullableDouble(table.LINE_DROP_R.queryIndex, tapChangerControl.lineDropR)
        insert.setNullableDouble(table.LINE_DROP_X.queryIndex, tapChangerControl.lineDropX)
        insert.setNullableDouble(table.REVERSE_LINE_DROP_R.queryIndex, tapChangerControl.reverseLineDropR)
        insert.setNullableDouble(table.REVERSE_LINE_DROP_X.queryIndex, tapChangerControl.reverseLineDropX)
        insert.setNullableBoolean(table.FORWARD_LDC_BLOCKING.queryIndex, tapChangerControl.forwardLDCBlocking)
        insert.setNullableDouble(table.TIME_DELAY.queryIndex, tapChangerControl.timeDelay)
        insert.setNullableBoolean(table.CO_GENERATION_ENABLED.queryIndex, tapChangerControl.coGenerationEnabled)

        return saveRegulatingControl(table, insert, tapChangerControl, "tap changer control")
    }

    @Throws(SQLException::class)
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

    /**
     * Save the [TransformerStarImpedance] fields to [TableTransformerStarImpedances].
     *
     * @param transformerStarImpedance The [TransformerStarImpedance] instance to write to the database.
     *
     * @return true if the [TransformerStarImpedance] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(transformerStarImpedance: TransformerStarImpedance): Boolean {
        val table = databaseTables.getTable<TableTransformerStarImpedances>()
        val insert = databaseTables.getInsert<TableTransformerStarImpedances>()

        insert.setNullableDouble(table.R.queryIndex, transformerStarImpedance.r)
        insert.setNullableDouble(table.R0.queryIndex, transformerStarImpedance.r0)
        insert.setNullableDouble(table.X.queryIndex, transformerStarImpedance.x)
        insert.setNullableDouble(table.X0.queryIndex, transformerStarImpedance.x0)
        insert.setNullableString(table.TRANSFORMER_END_INFO_MRID.queryIndex, transformerStarImpedance.transformerEndInfo?.mRID)

        return saveIdentifiedObject(table, insert, transformerStarImpedance, "transformer star impedance")
    }

    // ###############################
    // # IEC61970 InfIEC61970 Feeder #
    // ###############################

    /**
     * Save the [Circuit] fields to [TableCircuits].
     *
     * @param circuit The [Circuit] instance to write to the database.
     *
     * @return true if the [Circuit] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(circuit: Circuit): Boolean {
        val table = databaseTables.getTable<TableCircuits>()
        val insert = databaseTables.getInsert<TableCircuits>()

        insert.setNullableString(table.LOOP_MRID.queryIndex, circuit.loop?.mRID)

        var status = true
        circuit.endSubstations.forEach { status = status and saveAssociation(circuit, it) }
        circuit.endTerminals.forEach { status = status and saveAssociation(circuit, it) }

        return status and saveLine(table, insert, circuit, "circuit")
    }

    /**
     * Save the [Loop] fields to [TableLoops].
     *
     * @param loop The [Loop] instance to write to the database.
     *
     * @return true if the [Loop] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(loop: Loop): Boolean {
        val table = databaseTables.getTable<TableLoops>()
        val insert = databaseTables.getInsert<TableLoops>()

        var status = true
        loop.energizingSubstations.forEach { status = status and saveAssociation(loop, it, LoopSubstationRelationship.SUBSTATION_ENERGIZES_LOOP) }
        loop.substations.forEach { status = status and saveAssociation(loop, it, LoopSubstationRelationship.LOOP_ENERGIZES_SUBSTATION) }

        return status and saveIdentifiedObject(table, insert, loop, "loop")
    }

    /**
     * Save the [LvFeeder] fields to [TableLvFeeders].
     *
     * @param lvFeeder The [LvFeeder] instance to write to the database.
     *
     * @return true if the [LvFeeder] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(lvFeeder: LvFeeder): Boolean {
        val table = databaseTables.getTable<TableLvFeeders>()
        val insert = databaseTables.getInsert<TableLvFeeders>()

        insert.setNullableString(table.NORMAL_HEAD_TERMINAL_MRID.queryIndex, lvFeeder.normalHeadTerminal?.mRID)

        return saveEquipmentContainer(table, insert, lvFeeder, "lv feeder")
    }

    // ####################################################
    // # IEC61970 infIEC61970 Wires Generation Production #
    // ####################################################

    /**
     * Save the [EvChargingUnit] fields to [TableEvChargingUnits].
     *
     * @param evChargingUnit The [EvChargingUnit] instance to write to the database.
     *
     * @return true if the [EvChargingUnit] was successfully written to the database, otherwise false.
     * @throws SQLException For any errors encountered writing to the database.
     */
    @Throws(SQLException::class)
    fun save(evChargingUnit: EvChargingUnit): Boolean {
        val table = databaseTables.getTable<TableEvChargingUnits>()
        val insert = databaseTables.getInsert<TableEvChargingUnits>()

        return savePowerElectronicsUnit(table, insert, evChargingUnit, "ev charging unit")
    }

    // ################
    // # ASSOCIATIONS #
    // ################

    @Throws(SQLException::class)
    private fun saveAssociation(assetOrganisationRole: AssetOrganisationRole, asset: Asset): Boolean {
        val table = databaseTables.getTable<TableAssetOrganisationRolesAssets>()
        val insert = databaseTables.getInsert<TableAssetOrganisationRolesAssets>()

        insert.setString(table.ASSET_ORGANISATION_ROLE_MRID.queryIndex, assetOrganisationRole.mRID)
        insert.setString(table.ASSET_MRID.queryIndex, asset.mRID)

        return insert.tryExecuteSingleUpdate("asset organisation role to asset association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(usagePoint: UsagePoint, endDevice: EndDevice): Boolean {
        val table = databaseTables.getTable<TableUsagePointsEndDevices>()
        val insert = databaseTables.getInsert<TableUsagePointsEndDevices>()

        insert.setString(table.USAGE_POINT_MRID.queryIndex, usagePoint.mRID)
        insert.setString(table.END_DEVICE_MRID.queryIndex, endDevice.mRID)

        return insert.tryExecuteSingleUpdate("usage point to end device association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(equipment: Equipment, usagePoint: UsagePoint): Boolean {
        val table = databaseTables.getTable<TableEquipmentUsagePoints>()
        val insert = databaseTables.getInsert<TableEquipmentUsagePoints>()

        insert.setString(table.EQUIPMENT_MRID.queryIndex, equipment.mRID)
        insert.setString(table.USAGE_POINT_MRID.queryIndex, usagePoint.mRID)

        return insert.tryExecuteSingleUpdate("equipment to usage point association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(equipment: Equipment, operationalRestriction: OperationalRestriction): Boolean {
        val table = databaseTables.getTable<TableEquipmentOperationalRestrictions>()
        val insert = databaseTables.getInsert<TableEquipmentOperationalRestrictions>()

        insert.setString(table.EQUIPMENT_MRID.queryIndex, equipment.mRID)
        insert.setString(table.OPERATIONAL_RESTRICTION_MRID.queryIndex, operationalRestriction.mRID)

        return insert.tryExecuteSingleUpdate("equipment to operational restriction association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(equipment: Equipment, equipmentContainer: EquipmentContainer): Boolean {
        val table = databaseTables.getTable<TableEquipmentEquipmentContainers>()
        val insert = databaseTables.getInsert<TableEquipmentEquipmentContainers>()

        insert.setString(table.EQUIPMENT_MRID.queryIndex, equipment.mRID)
        insert.setString(table.EQUIPMENT_CONTAINER_MRID.queryIndex, equipmentContainer.mRID)

        return insert.tryExecuteSingleUpdate("equipment to equipment container association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(circuit: Circuit, substation: Substation): Boolean {
        val table = databaseTables.getTable<TableCircuitsSubstations>()
        val insert = databaseTables.getInsert<TableCircuitsSubstations>()

        insert.setString(table.CIRCUIT_MRID.queryIndex, circuit.mRID)
        insert.setString(table.SUBSTATION_MRID.queryIndex, substation.mRID)

        return insert.tryExecuteSingleUpdate("circuit to substation association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(circuit: Circuit, terminal: Terminal): Boolean {
        val table = databaseTables.getTable<TableCircuitsTerminals>()
        val insert = databaseTables.getInsert<TableCircuitsTerminals>()

        insert.setString(table.CIRCUIT_MRID.queryIndex, circuit.mRID)
        insert.setString(table.TERMINAL_MRID.queryIndex, terminal.mRID)

        return insert.tryExecuteSingleUpdate("circuit to terminal association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(loop: Loop, substation: Substation, relationship: LoopSubstationRelationship): Boolean {
        val table = databaseTables.getTable<TableLoopsSubstations>()
        val insert = databaseTables.getInsert<TableLoopsSubstations>()

        insert.setString(table.LOOP_MRID.queryIndex, loop.mRID)
        insert.setString(table.SUBSTATION_MRID.queryIndex, substation.mRID)
        insert.setString(table.RELATIONSHIP.queryIndex, relationship.name)

        return insert.tryExecuteSingleUpdate("loop to substation association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(protectionRelayFunction: ProtectionRelayFunction, protectedSwitch: ProtectedSwitch): Boolean {
        val table = databaseTables.getTable<TableProtectionRelayFunctionsProtectedSwitches>()
        val insert = databaseTables.getInsert<TableProtectionRelayFunctionsProtectedSwitches>()

        insert.setString(table.PROTECTION_RELAY_FUNCTION_MRID.queryIndex, protectionRelayFunction.mRID)
        insert.setString(table.PROTECTED_SWITCH_MRID.queryIndex, protectedSwitch.mRID)

        return insert.tryExecuteSingleUpdate("protection relay function to protected switch association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(protectionRelayFunction: ProtectionRelayFunction, sensor: Sensor): Boolean {
        val table = databaseTables.getTable<TableProtectionRelayFunctionsSensors>()
        val insert = databaseTables.getInsert<TableProtectionRelayFunctionsSensors>()

        insert.setString(table.PROTECTION_RELAY_FUNCTION_MRID.queryIndex, protectionRelayFunction.mRID)
        insert.setString(table.SENSOR_MRID.queryIndex, sensor.mRID)

        return insert.tryExecuteSingleUpdate("protection relay function to sensor association")
    }

    @Throws(SQLException::class)
    private fun saveAssociation(protectionRelayScheme: ProtectionRelayScheme, protectionRelayFunction: ProtectionRelayFunction): Boolean {
        val table = databaseTables.getTable<TableProtectionRelaySchemesProtectionRelayFunctions>()
        val insert = databaseTables.getInsert<TableProtectionRelaySchemesProtectionRelayFunctions>()

        insert.setString(table.PROTECTION_RELAY_SCHEME_MRID.queryIndex, protectionRelayScheme.mRID)
        insert.setString(table.PROTECTION_RELAY_FUNCTION_MRID.queryIndex, protectionRelayFunction.mRID)

        return insert.tryExecuteSingleUpdate("protection relay function to protection relay function association")
    }

}
