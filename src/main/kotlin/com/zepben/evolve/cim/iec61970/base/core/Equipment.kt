/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.cim.iec61970.base.core

import com.zepben.evolve.cim.iec61968.metering.UsagePoint
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.services.common.extensions.asUnmodifiable
import com.zepben.evolve.services.common.extensions.getByMRID
import com.zepben.evolve.services.common.extensions.validateReference

/**
 * @property inService If true, the equipment is in service.
 * @property normallyInService If true, the equipment is _normally_ in service.
 *
 * @property sites [Site]'s this equipment belongs to.
 * @property normalFeeders [Feeder]'- that represent the normal feeders of the equipment.
 * @property currentFeeders [Feeder]'s that represent the current feeders of the equipment.
 * @property substations [Substation]'s that represent the substation of the equipment.
 */
abstract class Equipment(mRID: String = "") : PowerSystemResource(mRID) {

    var inService: Boolean = true
    var normallyInService: Boolean = true
    private var _equipmentContainers: MutableList<EquipmentContainer>? = null
    private var _usagePoints: MutableList<UsagePoint>? = null
    private var _operationalRestrictions: MutableList<OperationalRestriction>? = null
    private var _currentFeeders: MutableList<Feeder>? = null

    val sites: List<Site> get() = equipmentContainersOfType()
    val normalFeeders: List<Feeder> get() = equipmentContainersOfType()
    val substations: List<Substation> get() = equipmentContainersOfType()
    val circuits: List<Circuit> get() = equipmentContainersOfType()

    /**
     * The equipment containers this equipment belongs to. The returned collection is read only.
     */
    val containers: Collection<EquipmentContainer> get() = _equipmentContainers.asUnmodifiable()

    /**
     * Get the number of entries in the [EquipmentContainer] collection.
     */
    fun numContainers() = _equipmentContainers?.size ?: 0

    /**
     * [EquipmentContainer]'s that this equipment is associated with.
     *
     * @param mRID the mRID of the required [EquipmentContainer]
     * @return The [EquipmentContainer] with the specified [mRID] if it exists, otherwise null
     */
    fun getContainer(mRID: String) = _equipmentContainers.getByMRID(mRID)

    /**
     * @param equipmentContainer the equipment container to associate with this equipment.
     * @return A reference to this [Equipment] to allow fluent use.
     */
    fun addContainer(equipmentContainer: EquipmentContainer): Equipment {
        if (validateReference(equipmentContainer, ::getContainer, "An EquipmentContainer"))
            return this

        _equipmentContainers = _equipmentContainers ?: mutableListOf()
        _equipmentContainers!!.add(equipmentContainer)

        return this
    }

    /**
     * @param equipmentContainer the equipment container to disassociate with this equipment.
     * @return `true` if [equipmentContainer] has been successfully removed; `false` if it was not present in the set.
     */
    fun removeContainer(equipmentContainer: EquipmentContainer?): Boolean {
        val ret = _equipmentContainers?.remove(equipmentContainer) == true
        if (_equipmentContainers.isNullOrEmpty()) _equipmentContainers = null
        return ret
    }

    /**
     * Clear this [Equipment]'s associated [EquipmentContainer]'s
     * @return this [Equipment]
     */
    fun clearContainers(): Equipment {
        _equipmentContainers = null
        return this
    }

    val currentFeeders: Collection<Feeder> get() = _currentFeeders.asUnmodifiable()

    /**
     * Get the number of entries in the current [Feeder] collection.
     */
    fun numCurrentFeeders() = _currentFeeders?.size ?: 0

    /**
     * [Feeder]'s that represent the current feeders of the equipment.
     *
     * @param mRID the mRID of the required current [Feeder]
     * @return The current [Feeder] with the specified [mRID] if it exists, otherwise null
     */
    fun getCurrentFeeder(mRID: String) = _currentFeeders.getByMRID(mRID)

    /**
     * @param feeder the equipment container to associate with this equipment.
     * @return A reference to this [Equipment] to allow fluent use.
     */
    fun addCurrentFeeder(feeder: Feeder): Equipment {
        if (validateReference(feeder, ::getCurrentFeeder, "A current Feeder"))
            return this

        _currentFeeders = _currentFeeders ?: mutableListOf()
        _currentFeeders!!.add(feeder)

        return this
    }

    /**
     * @param feeder the equipment container to disassociate with this equipment.
     * @return `true` if [feeder] has been successfully removed; `false` if it was not present in the set.
     */
    fun removeCurrentFeeder(feeder: Feeder?): Boolean {
        val ret = _currentFeeders?.remove(feeder) == true
        if (_currentFeeders.isNullOrEmpty()) _currentFeeders = null
        return ret
    }

    /**
     * Clear this [Equipment]'s associated current [EquipmentContainer]'s
     * @return this [Equipment]
     */
    fun clearCurrentFeeders(): Equipment {
        _currentFeeders = null
        return this
    }

    /**
     * The usage points for this equipment. The returned collection is read only.
     */
    val usagePoints: List<UsagePoint> get() = _usagePoints.asUnmodifiable()

    /**
     * Get the number of entries in the [UsagePoint] collection.
     */
    fun numUsagePoints() = _usagePoints?.size ?: 0

    /**
     * [UsagePoint]'s connected to the electrical grid through this equipment.
     *
     * @param mRID the mRID of the required [UsagePoint]
     * @return The [UsagePoint] with the specified [mRID] if it exists, otherwise null
     */
    fun getUsagePoint(mRID: String) = _usagePoints.getByMRID(mRID)

    /**
     * @param usagePoint the usage point that connects to the electrical grid through this equipment.
     * @return true if the usage point is associated.
     * @return A reference to this [Equipment] to allow fluent use.
     */
    fun addUsagePoint(usagePoint: UsagePoint): Equipment {
        if (validateReference(usagePoint, ::getUsagePoint, "A UsagePoint"))
            return this

        _usagePoints = _usagePoints ?: mutableListOf()
        _usagePoints!!.add(usagePoint)

        return this
    }

    /**
     * @param usagePoint the usage point to disconnect from this equipment.
     * @return this [Equipment]
     */
    fun removeUsagePoint(usagePoint: UsagePoint?): Boolean {
        val ret = _usagePoints?.remove(usagePoint) == true
        if (_usagePoints.isNullOrEmpty()) _usagePoints = null
        return ret
    }

    /**
     * Clear this [Equipment]'s associated [UsagePoint]'s
     * @return this [Equipment]
     */
    fun clearUsagePoints(): Equipment {
        _usagePoints = null
        return this
    }

    /**
     *  [OperationalRestriction]'s that this equipment is associated with. The returned collection is read only.
     */
    val operationalRestrictions: Collection<OperationalRestriction> get() = _operationalRestrictions.asUnmodifiable()

    /**
     * Get the number of entries in the [OperationalRestriction] collection.
     */
    fun numOperationalRestrictions() = _operationalRestrictions?.size ?: 0

    /**
     * [OperationalRestriction]'s that this equipment is associated with.
     *
     * @param mRID the mRID of the required [OperationalRestriction]
     * @return The [OperationalRestriction] with the specified [mRID] if it exists, otherwise null
     */
    fun getOperationalRestriction(mRID: String) = _operationalRestrictions.getByMRID(mRID)

    /**
     * Add an operational restriction that applies to this equipment.
     *
     * @param restriction The operational restriction that applies to this equipment.
     * @return true if the operation restriction was added.
     * @return A reference to this [Equipment] to allow fluent use.
     */
    fun addOperationalRestriction(restriction: OperationalRestriction): Equipment {
        if (validateReference(restriction, ::getOperationalRestriction, "An OperationalRestriction"))
            return this

        _operationalRestrictions = _operationalRestrictions ?: mutableListOf()
        _operationalRestrictions!!.add(restriction)

        return this
    }

    /**
     * Removes an operational restriction that has been associated with this equipment.
     * @param restriction The operational restriction to be removed.
     * @return this [Equipment]
     */
    fun removeOperationalRestriction(restriction: OperationalRestriction?): Boolean {
        val ret = _operationalRestrictions?.remove(restriction) == true
        if (_operationalRestrictions.isNullOrEmpty()) _operationalRestrictions = null
        return ret
    }

    /**
     * Clear this [Equipment]'s associated [OperationalRestriction]'s
     * @return this [Equipment]
     */
    fun clearOperationalRestrictions(): Equipment {
        _operationalRestrictions = null
        return this
    }

    private inline fun <reified T : EquipmentContainer> equipmentContainersOfType(): List<T> {
        return _equipmentContainers?.filterIsInstance(T::class.java) ?: emptyList()
    }
}
