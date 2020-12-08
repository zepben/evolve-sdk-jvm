/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.streaming.get

import com.zepben.evolve.cim.iec61970.base.core.ConductingEquipment
import com.zepben.evolve.cim.iec61970.base.core.Feeder
import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.wires.AcLineSegment
import com.zepben.evolve.cim.iec61970.base.wires.Conductor
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.Resolvers
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.common.translator.mRID
import com.zepben.evolve.services.measurement.mRID
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.translator.NetworkProtoToCim
import com.zepben.evolve.services.network.translator.mRID
import com.zepben.evolve.streaming.get.hierarchy.*
import com.zepben.evolve.streaming.grpc.GrpcResult
import com.zepben.protobuf.nc.GetIdentifiedObjectsRequest
import com.zepben.protobuf.nc.GetNetworkHierarchyRequest
import com.zepben.protobuf.nc.NetworkConsumerGrpc
import com.zepben.protobuf.nc.NetworkIdentifiedObject
import com.zepben.protobuf.nc.NetworkIdentifiedObject.IdentifiedObjectCase.*
import io.grpc.Channel


/**
 * Consumer client for a [NetworkService].
 *
 * @property stub The gRPC stub to be used to communicate with the server
 */
class NetworkConsumerClient(
    private val stub: NetworkConsumerGrpc.NetworkConsumerBlockingStub,
    private val protoToCimProvider: (NetworkService) -> NetworkProtoToCim = { NetworkProtoToCim(it) }
) : CimConsumerClient<NetworkService>() {

    constructor(channel: Channel) : this(NetworkConsumerGrpc.newBlockingStub(channel))

    /**
     * Retrieve the object with the given [mRID] and store the result in the [service].
     *
     * Exceptions that occur during sending will be caught and passed to all error handlers that have been registered by [addErrorHandler].
     *
     * @return A [GrpcResult] with a result of one of the following:
     * - The item if found
     * - null if an object could not be found or it was found but not added to [service] (see [BaseService.add]).
     * - A [Throwable] if an error occurred while retrieving or processing the object, in which case, [GrpcResult.wasSuccessful] will return false.
     */
    override fun getIdentifiedObject(service: NetworkService, mRID: String): GrpcResult<IdentifiedObject?> {
        return tryRpc {
            processIdentifiedObjects(service, setOf(mRID)).firstOrNull()?.identifiedObject
        }
    }

    /**
     * Retrieve the objects with the given [mRIDs] and store the results in the [service].
     *
     * Exceptions that occur during processing will be caught and passed to all error handlers that have been registered by [addErrorHandler].
     *
     * WARNING: This operation is not atomic upon [service], and thus if processing fails partway through [mRIDs], any previously successful mRID will have been
     * added to the service, and thus you may have an incomplete [NetworkService]. Also note that adding to the [service] may not occur for an object if another
     * object with the same mRID is already present in [service]. [MultiObjectResult.failed] can be used to check for mRIDs that were retrieved but not
     * added to [service].
     *
     * @return A [GrpcResult] with a result of one of the following:
     * - A [MultiObjectResult] containing a map of the retrieved objects keyed by mRID. If an item is not found it will be excluded from the map.
     * If an item couldn't be added to [service], its mRID will be present in [MultiObjectResult.failed] (see [BaseService.add]).
     * - A [Throwable] if an error occurred while retrieving or processing the objects, in which case, [GrpcResult.wasSuccessful] will return false.
     * Note the warning above in this case.
     */
    override fun getIdentifiedObjects(service: NetworkService, mRIDs: Iterable<String>): GrpcResult<MultiObjectResult> {
        return tryRpc {
            processIdentifiedObjects(service, mRIDs.toSet()).let { extracted ->
                val results = mutableMapOf<String, IdentifiedObject>()
                val failed = mutableSetOf<String>()
                extracted.forEach {
                    if (it.identifiedObject == null) failed.add(it.mRID) else results[it.identifiedObject.mRID] = it.identifiedObject
                }
                MultiObjectResult(results, failed)
            }
        }
    }

    /***
     * Retrieve the network hierarchy.
     *
     * @return A simplified version of the network hierarchy that can be used to make further in-depth requests.
     */
    fun getNetworkHierarchy(): GrpcResult<NetworkHierarchy> {
        return tryRpc {
            val response = stub.getNetworkHierarchy(GetNetworkHierarchyRequest.newBuilder().build())

            val feeders = toMap(response.feedersList) { NetworkHierarchyFeeder(it.mrid, it.name) }
            val substations = toMap(response.substationsList) { NetworkHierarchySubstation(it.mrid, it.name, lookup(it.feederMridsList, feeders)) }
            val subGeographicalRegions = toMap(response.subGeographicalRegionsList) {
                NetworkHierarchySubGeographicalRegion(it.mrid, it.name, lookup(it.substationMridsList, substations))
            }
            val geographicalRegions = toMap(response.geographicalRegionsList) {
                NetworkHierarchyGeographicalRegion(it.mrid, it.name, lookup(it.subGeographicalRegionMridsList, subGeographicalRegions))
            }

            finaliseLinks(geographicalRegions, subGeographicalRegions, substations)
            NetworkHierarchy(geographicalRegions, subGeographicalRegions, substations, feeders)
        }
    }

    /***
     * Retrieve the feeder network for the specified [mRID] and store the results in the [service].
     *
     * This is a convenience method that will fetch the feeder object, all of the equipment referenced by the feeder (normal state),
     * the terminals of all elements, the connectivity between terminals, the locations of all elements, the ends of all transformers
     * and the wire info for all conductors.
     *
     * @return A GrpcResult of a [MultiObjectResult], containing a map keyed by mRID of all the objects retrieved as part of retrieving the [Feeder] and the
     * [Feeder] itself. If an item couldn't be added to [service], its mRID will be present in [MultiObjectResult.failed].
     */
    fun getFeeder(service: NetworkService, mRID: String): GrpcResult<MultiObjectResult> {
        val feederResponse = getIdentifiedObject(service, mRID)
        val feeder = feederResponse.onError { thrown, wasHandled -> return@getFeeder GrpcResult.ofError(thrown, wasHandled) }.value

        if (feeder == null)
            return GrpcResult.of(null)
        else if (feeder !is Feeder)
            return GrpcResult.ofError(ClassCastException("Unable to extract feeder network from ${feeder.typeNameAndMRID()}."), false)

        val equipmentResults = getIdentifiedObjects(service, service.getUnresolvedReferenceMrids(Resolvers.equipment(feeder)))
            .onError { thrown, wasHandled -> return@getFeeder GrpcResult.ofError(thrown, wasHandled) }

        val mRIDs = service.getUnresolvedReferenceMrids(Resolvers.normalEnergizingSubstation(feeder)).toMutableSet()

        feeder.equipment.forEach {
            if (it is ConductingEquipment) {
                it.terminals.forEach { terminal ->
                    mRIDs.addAll(service.getUnresolvedReferenceMrids(Resolvers.connectivityNode(terminal)))
                }
            }

            if (it is Conductor) {
                if (it is AcLineSegment)
                    mRIDs.addAll(service.getUnresolvedReferenceMrids(Resolvers.perLengthSequenceImpedance(it)))
                mRIDs.addAll(service.getUnresolvedReferenceMrids(Resolvers.assetInfo(it)))
            }

            mRIDs.addAll(service.getUnresolvedReferenceMrids(Resolvers.location(it)))
        }

        return GrpcResult.of(
            getIdentifiedObjects(service, mRIDs).onError { thrown, wasHandled -> return@getFeeder GrpcResult.ofError(thrown, wasHandled) }.value.let {
                MultiObjectResult(
                    it.objects.toMutableMap().apply { this[feeder.mRID] = feeder; this.putAll(equipmentResults.value.objects) },
                    it.failed.union(equipmentResults.value.failed)
                )
            }
        )
    }

    private fun processIdentifiedObjects(service: NetworkService, mRIDs: Set<String>): Sequence<ExtractResult> {
        val toFetch = mutableSetOf<String>()
        val existing = mutableSetOf<ExtractResult>()
        mRIDs.forEach { mRID ->  // Only process mRIDs not already present in service
            service.get<IdentifiedObject>(mRID)?.let { existing.add(ExtractResult(it, it.mRID)) } ?: toFetch.add(mRID)
        }

        return stub.getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(toFetch).build())
            .asSequence()
            .map { it.objectGroup }
            .flatMap {
                sequenceOf(extractIdentifiedObject(service, it.identifiedObject)) +
                    it.ownedIdentifiedObjectList.map { owned -> extractIdentifiedObject(service, owned) }
            } + existing
    }

    private fun extractIdentifiedObject(service: NetworkService, it: NetworkIdentifiedObject): ExtractResult {
        return when (it.identifiedObjectCase) {
            CABLEINFO -> ExtractResult(protoToCimProvider(service).addFromPb(it.cableInfo), it.cableInfo.mRID())
            OVERHEADWIREINFO -> ExtractResult(protoToCimProvider(service).addFromPb(it.overheadWireInfo), it.overheadWireInfo.mRID())
            ASSETOWNER -> ExtractResult(protoToCimProvider(service).addFromPb(it.assetOwner), it.assetOwner.mRID())
            ORGANISATION -> ExtractResult(protoToCimProvider(service).addFromPb(it.organisation), it.organisation.mRID())
            LOCATION -> ExtractResult(protoToCimProvider(service).addFromPb(it.location), it.location.mRID())
            METER -> ExtractResult(protoToCimProvider(service).addFromPb(it.meter), it.meter.mRID())
            USAGEPOINT -> ExtractResult(protoToCimProvider(service).addFromPb(it.usagePoint), it.usagePoint.mRID())
            OPERATIONALRESTRICTION -> ExtractResult(protoToCimProvider(service).addFromPb(it.operationalRestriction), it.operationalRestriction.mRID())
            FAULTINDICATOR -> ExtractResult(protoToCimProvider(service).addFromPb(it.faultIndicator), it.faultIndicator.mRID())
            BASEVOLTAGE -> ExtractResult(protoToCimProvider(service).addFromPb(it.baseVoltage), it.baseVoltage.mRID())
            CONNECTIVITYNODE -> ExtractResult(protoToCimProvider(service).addFromPb(it.connectivityNode), it.connectivityNode.mRID())
            FEEDER -> ExtractResult(protoToCimProvider(service).addFromPb(it.feeder), it.feeder.mRID())
            GEOGRAPHICALREGION -> ExtractResult(protoToCimProvider(service).addFromPb(it.geographicalRegion), it.geographicalRegion.mRID())
            SITE -> ExtractResult(protoToCimProvider(service).addFromPb(it.site), it.site.mRID())
            SUBGEOGRAPHICALREGION -> ExtractResult(protoToCimProvider(service).addFromPb(it.subGeographicalRegion), it.subGeographicalRegion.mRID())
            SUBSTATION -> ExtractResult(protoToCimProvider(service).addFromPb(it.substation), it.substation.mRID())
            TERMINAL -> ExtractResult(protoToCimProvider(service).addFromPb(it.terminal), it.terminal.mRID())
            ACLINESEGMENT -> ExtractResult(protoToCimProvider(service).addFromPb(it.acLineSegment), it.acLineSegment.mRID())
            BREAKER -> ExtractResult(protoToCimProvider(service).addFromPb(it.breaker), it.breaker.mRID())
            DISCONNECTOR -> ExtractResult(protoToCimProvider(service).addFromPb(it.disconnector), it.disconnector.mRID())
            ENERGYCONSUMER -> ExtractResult(protoToCimProvider(service).addFromPb(it.energyConsumer), it.energyConsumer.mRID())
            ENERGYCONSUMERPHASE -> ExtractResult(protoToCimProvider(service).addFromPb(it.energyConsumerPhase), it.energyConsumerPhase.mRID())
            ENERGYSOURCE -> ExtractResult(protoToCimProvider(service).addFromPb(it.energySource), it.energySource.mRID())
            ENERGYSOURCEPHASE -> ExtractResult(protoToCimProvider(service).addFromPb(it.energySourcePhase), it.energySourcePhase.mRID())
            FUSE -> ExtractResult(protoToCimProvider(service).addFromPb(it.fuse), it.fuse.mRID())
            JUMPER -> ExtractResult(protoToCimProvider(service).addFromPb(it.jumper), it.jumper.mRID())
            JUNCTION -> ExtractResult(protoToCimProvider(service).addFromPb(it.junction), it.junction.mRID())
            LINEARSHUNTCOMPENSATOR -> ExtractResult(protoToCimProvider(service).addFromPb(it.linearShuntCompensator), it.linearShuntCompensator.mRID())
            PERLENGTHSEQUENCEIMPEDANCE -> ExtractResult(
                protoToCimProvider(service).addFromPb(it.perLengthSequenceImpedance),
                it.perLengthSequenceImpedance.mRID()
            )
            POWERTRANSFORMER -> ExtractResult(protoToCimProvider(service).addFromPb(it.powerTransformer), it.powerTransformer.mRID())
            POWERTRANSFORMEREND -> ExtractResult(protoToCimProvider(service).addFromPb(it.powerTransformerEnd), it.powerTransformerEnd.mRID())
            RATIOTAPCHANGER -> ExtractResult(protoToCimProvider(service).addFromPb(it.ratioTapChanger), it.ratioTapChanger.mRID())
            RECLOSER -> ExtractResult(protoToCimProvider(service).addFromPb(it.recloser), it.recloser.mRID())
            CIRCUIT -> ExtractResult(protoToCimProvider(service).addFromPb(it.circuit), it.circuit.mRID())
            LOOP -> ExtractResult(protoToCimProvider(service).addFromPb(it.loop), it.loop.mRID())
            POLE -> ExtractResult(protoToCimProvider(service).addFromPb(it.pole), it.pole.mRID())
            STREETLIGHT -> ExtractResult(protoToCimProvider(service).addFromPb(it.streetlight), it.streetlight.mRID())
            ACCUMULATOR -> ExtractResult(protoToCimProvider(service).addFromPb(it.accumulator), it.accumulator.measurement.mRID())
            ANALOG -> ExtractResult(protoToCimProvider(service).addFromPb(it.analog), it.analog.measurement.mRID())
            DISCRETE -> ExtractResult(protoToCimProvider(service).addFromPb(it.discrete), it.discrete.measurement.mRID())
            CONTROL -> ExtractResult(protoToCimProvider(service).addFromPb(it.control), it.control.mRID())
            REMOTECONTROL -> ExtractResult(protoToCimProvider(service).addFromPb(it.remoteControl), it.remoteControl.mRID())
            REMOTESOURCE -> ExtractResult(protoToCimProvider(service).addFromPb(it.remoteSource), it.remoteSource.mRID())
            OTHER, IDENTIFIEDOBJECT_NOT_SET, null -> throw UnsupportedOperationException("Identified object type ${it.identifiedObjectCase} is not supported by the network service")
        }
    }

    private fun <T, U : NetworkHierarchyIdentifiedObject> toMap(objects: Iterable<T>, mapper: (T) -> U): Map<String, U> = objects
        .map(mapper)
        .associateBy { it.mRID }

    private fun <T : NetworkHierarchyIdentifiedObject> lookup(mRIDs: Iterable<String>, lookup: Map<String, T>): Map<String, T> =
        mRIDs.mapNotNull { mRID -> lookup[mRID] }
            .associateBy { it.mRID }

    private fun finaliseLinks(
        geographicalRegions: Map<String, NetworkHierarchyGeographicalRegion>,
        subGeographicalRegions: Map<String, NetworkHierarchySubGeographicalRegion>,
        substations: Map<String, NetworkHierarchySubstation>
    ) {
        geographicalRegions.values.forEach { it.subGeographicalRegions.values.forEach { other -> other.geographicalRegion = it } }
        subGeographicalRegions.values.forEach { it.substations.values.forEach { other -> other.subGeographicalRegion = it } }
        substations.values.forEach { it.feeders.values.forEach { other -> other.substation = it } }
    }

}