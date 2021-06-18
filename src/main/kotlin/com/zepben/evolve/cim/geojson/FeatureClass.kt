/*
 * Copyright Zeppelin Bend Pty Ltd (Zepben). The use of this file and its contents requires explicit permission from Zepben.
 */

package com.zepben.evolve.cim.geojson

import com.zepben.protobuf.cim.iec61970.base.core.IdentifiedObject
import kotlin.reflect.KClass


enum class FeatureClass {
    AcLineSegment,
    BatteryUnit,
    Breaker,
    CableInfo,
    Disconnector,
    Jumper,
    Junction,
    BusbarSection,
    Fuse,
    FaultIndicator,
    LoadBreakSwitch,
    OverheadWireInfo,
    PowerTransformer,
    Recloser,
    UsagePoint,
    EnergyConsumer,
    EnergySource,
    LinearShuntCompensator,
    Meter,
    PerLengthSequenceImpedance,
    PhotoVoltaicUnit,
    PowerElectronicsWindUnit,

    HV_FEEDER,
    METADATA;

    fun isSwitch(): Boolean =
        when (this) {
            Breaker, Disconnector, Jumper, Fuse, LoadBreakSwitch, Recloser -> true
            else -> false
        }

}