/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.measurement.translator

import com.zepben.evolve.cim.iec61970.base.meas.*
import com.zepben.evolve.services.common.translator.toInstant
import com.zepben.evolve.services.measurement.MeasurementService
import com.zepben.protobuf.cim.iec61970.base.meas.AccumulatorValue as PBAccumulatorValue
import com.zepben.protobuf.cim.iec61970.base.meas.AnalogValue as PBAnalogValue
import com.zepben.protobuf.cim.iec61970.base.meas.DiscreteValue as PBDiscreteValue
import com.zepben.protobuf.cim.iec61970.base.meas.MeasurementValue as PBMeasurementValue

/************ IEC61970 MEAS ************/
fun toCim(pb: PBMeasurementValue, cim: MeasurementValue): MeasurementValue =
    cim.apply { timeStamp = pb.timeStamp.toInstant() }

fun toCim(pb: PBAnalogValue): AnalogValue =
    AnalogValue().apply {
        analogMRID = pb.analogMRID.takeIf { it.isNotBlank() }
        value = pb.value
        toCim(pb.mv, this)
    }

fun toCim(pb: PBAccumulatorValue): AccumulatorValue =
    AccumulatorValue().apply {
        accumulatorMRID = pb.accumulatorMRID.takeIf { it.isNotBlank() }
        value = pb.value.toUInt()
        toCim(pb.mv, this)
    }

fun toCim(pb: PBDiscreteValue): DiscreteValue =
    DiscreteValue().apply {
        discreteMRID = pb.discreteMRID.takeIf { it.isNotBlank() }
        value = pb.value
        toCim(pb.mv, this)
    }


/************ Extensions ************/

fun MeasurementService.addFromPb(pb: PBAnalogValue): AnalogValue = toCim(pb).also { add(it) }
fun MeasurementService.addFromPb(pb: PBAccumulatorValue): AccumulatorValue = toCim(pb).also { add(it) }
fun MeasurementService.addFromPb(pb: PBDiscreteValue): DiscreteValue = toCim(pb).also { add(it) }

/************ Class for Java friendly usage ************/

// This will be left unused until we have a measurement consumer client.
@Suppress("Unused")
class MeasurementProtoToCim(private val measurementService: MeasurementService) {
    fun addFromPb(pb: PBAnalogValue): AnalogValue = measurementService.addFromPb(pb)
    fun addFromPb(pb: PBAccumulatorValue): AccumulatorValue = measurementService.addFromPb(pb)
    fun addFromPb(pb: PBDiscreteValue): DiscreteValue = measurementService.addFromPb(pb)
}
