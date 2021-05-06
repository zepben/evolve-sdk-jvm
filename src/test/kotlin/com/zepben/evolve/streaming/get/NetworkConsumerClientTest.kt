/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.streaming.get

import com.nhaarman.mockitokotlin2.*
import com.zepben.evolve.cim.iec61968.assetinfo.CableInfo
import com.zepben.evolve.cim.iec61968.assetinfo.OverheadWireInfo
import com.zepben.evolve.cim.iec61968.assetinfo.WireInfo
import com.zepben.evolve.cim.iec61968.common.Location
import com.zepben.evolve.cim.iec61968.operations.OperationalRestriction
import com.zepben.evolve.cim.iec61970.base.core.*
import com.zepben.evolve.cim.iec61970.base.wires.*
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Circuit
import com.zepben.evolve.cim.iec61970.infiec61970.feeder.Loop
import com.zepben.evolve.services.common.extensions.typeNameAndMRID
import com.zepben.evolve.services.network.NetworkService
import com.zepben.evolve.services.network.NetworkServiceComparator
import com.zepben.evolve.services.network.translator.toPb
import com.zepben.evolve.streaming.get.ConsumerUtils.buildFromBuilder
import com.zepben.evolve.streaming.get.ConsumerUtils.forEachBuilder
import com.zepben.evolve.streaming.get.ConsumerUtils.validateFailure
import com.zepben.evolve.streaming.get.hierarchy.NetworkHierarchy
import com.zepben.evolve.streaming.get.testdata.*
import com.zepben.evolve.streaming.grpc.CaptureLastRpcErrorHandler
import com.zepben.evolve.streaming.grpc.GrpcResult
import com.zepben.protobuf.nc.*
import com.zepben.testutils.exception.ExpectException.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class NetworkConsumerClientTest {

    @JvmField
    @RegisterExtension
    var systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    private val stub = mock<NetworkConsumerGrpc.NetworkConsumerBlockingStub>()
    private val onErrorHandler = CaptureLastRpcErrorHandler()
    private val consumerClient = spy(NetworkConsumerClient(stub).apply { addErrorHandler(onErrorHandler) })
    private val service = NetworkService()

    @Test
    internal fun `can get all supported types`() {
        var counter = 0
        val builder = NetworkIdentifiedObject.newBuilder()

        forEachBuilder(builder) {
            val mRID = "id" + ++counter
            val response = createResponse(builder, it, mRID)

            doReturn(listOf(response).iterator()).`when`(stub).getIdentifiedObjects(any())

            val result = consumerClient.getIdentifiedObject(service, mRID)

            val type = response.identifiedObject.identifiedObjectCase
            if (isSupported(type)) {
                assertThat(result.wasSuccessful, equalTo(true))
                assertThat(result.value.mRID, equalTo(mRID))
            } else {
                assertThat(result.wasFailure, equalTo(true))
                assertThat(result.thrown, instanceOf(UnsupportedOperationException::class.java))
                assertThat(
                    result.thrown.message,
                    equalTo("Identified object type $type is not supported by the network service")
                )
                assertThat(result.thrown, equalTo(onErrorHandler.lastError))
            }

            verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build())
            clearInvocations(stub)
        }
    }

    @Test
    internal fun `returns error when object is not found`() {
        val mRID = "unknown"
        doReturn(listOf<GetIdentifiedObjectsResponse>().iterator()).`when`(stub).getIdentifiedObjects(any())

        val result = consumerClient.getIdentifiedObject(service, mRID)

        verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build())
        assertThat(result.wasFailure, equalTo(true))
        expect { throw result.thrown }
            .toThrow(NoSuchElementException::class.java)
            .withMessage("No object with mRID $mRID could be found.")
    }

    @Test
    internal fun `calls error handler when getting an IdentifiedObject throws`() {
        val mRID = "1234"
        val expectedEx = StatusRuntimeException(Status.UNAVAILABLE)
        doAnswer { throw expectedEx }.`when`(stub).getIdentifiedObjects(any())

        val result = consumerClient.getIdentifiedObject(service, mRID)

        verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build())
        validateFailure(onErrorHandler, result, expectedEx, true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting an IdentifiedObject throws`() {
        val mRID = "1234"
        val expectedEx = StatusRuntimeException(Status.UNAVAILABLE)
        doAnswer { throw expectedEx }.`when`(stub).getIdentifiedObjects(any())

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getIdentifiedObject(service, mRID)

        verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build())
        validateFailure(onErrorHandler, result, expectedEx, false)
    }

    @Test
    internal fun `can get multiple identified objects in single call`() {
        val mRIDs = listOf("id1", "id2", "id3")
        val response1 = createResponse(
            NetworkIdentifiedObject.newBuilder(),
            NetworkIdentifiedObject.Builder::getAcLineSegmentBuilder,
            mRIDs[0]
        )
        val response2 = createResponse(
            NetworkIdentifiedObject.newBuilder(),
            NetworkIdentifiedObject.Builder::getAcLineSegmentBuilder,
            mRIDs[1]
        )
        val response3 = createResponse(
            NetworkIdentifiedObject.newBuilder(),
            NetworkIdentifiedObject.Builder::getBreakerBuilder,
            mRIDs[2]
        )

        doReturn(listOf(response1, response2, response3).iterator()).`when`(stub).getIdentifiedObjects(any())

        val result = consumerClient.getIdentifiedObjects(service, mRIDs)

        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.objects.size, equalTo(3))
        assertThat(result.value.objects[mRIDs[0]], instanceOf(AcLineSegment::class.java))
        assertThat(result.value.objects[mRIDs[1]], instanceOf(AcLineSegment::class.java))
        assertThat(result.value.objects[mRIDs[2]], instanceOf(Breaker::class.java))

        verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build())
        clearInvocations(stub)
    }

    @Test
    internal fun `calls error handler when getting multiple IdentifiedObject throws`() {
        val mRIDs = listOf("id1", "id2", "id3")
        val expectedEx = StatusRuntimeException(Status.UNAVAILABLE)
        doAnswer { throw expectedEx }.`when`(stub).getIdentifiedObjects(any())

        val result = consumerClient.getIdentifiedObjects(service, mRIDs)

        verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build())
        validateFailure(onErrorHandler, result, expectedEx, true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting multiple IdentifiedObject throws`() {
        val mRIDs = listOf("id1", "id2", "id3")
        val expectedEx = StatusRuntimeException(Status.UNAVAILABLE)
        doAnswer { throw expectedEx }.`when`(stub).getIdentifiedObjects(any())

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getIdentifiedObjects(service, mRIDs)

        verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build())
        validateFailure(onErrorHandler, result, expectedEx, false)
    }

    @Test
    internal fun `can get network hierarchy`() {
        doReturn(NetworkHierarchyAllTypes.createResponse()).`when`(stub).getNetworkHierarchy(any())

        val result = consumerClient.getNetworkHierarchy(service)

        verify(stub).getNetworkHierarchy(GetNetworkHierarchyRequest.newBuilder().build())
        assertThat(result.wasSuccessful, equalTo(true))
        validateNetworkHierarchy(result.value, NetworkHierarchyAllTypes.createNetworkHierarchy())
    }

    @Test
    internal fun `calls error handler when getting the network hierarchy throws`() {
        val expectedEx = StatusRuntimeException(Status.UNAVAILABLE)
        doAnswer { throw expectedEx }.`when`(stub).getNetworkHierarchy(any())

        val result = consumerClient.getNetworkHierarchy(service)

        verify(stub).getNetworkHierarchy(GetNetworkHierarchyRequest.newBuilder().build())
        validateFailure(onErrorHandler, result, expectedEx, true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the network hierarchy throws`() {
        val expectedEx = StatusRuntimeException(Status.UNAVAILABLE)
        doAnswer { throw expectedEx }.`when`(stub).getNetworkHierarchy(any())

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getNetworkHierarchy(service)

        verify(stub).getNetworkHierarchy(GetNetworkHierarchyRequest.newBuilder().build())
        validateFailure(onErrorHandler, result, expectedEx, false)
    }

    @Test
    internal fun `can get feeder`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService)

        val mRID = "f001"
        val result = consumerClient.getEquipmentContainer<Feeder>(service, mRID)

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(3)).getIdentifiedObjects(any())

        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.objects.containsKey(mRID), equalTo(true))
        assertThat(result.value.objects.size, equalTo(21))

        result.value.objects.values.forEach { assertThat(service[it.mRID], equalTo(it)) }
        service.sequenceOf<IdentifiedObject>().forEach { assertThat(result.value.objects[it.mRID], equalTo(it)) }
        assertThat(result.value.failed, empty())

        val actualFeeder: Feeder = service[mRID]!!
        val expectedFeeder: Feeder = expectedService[mRID]!!

        assertThat(NetworkServiceComparator().compare(actualFeeder, expectedFeeder).differences, anEmptyMap())
    }

    @Test
    internal fun `handles missing feeder`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f002")

        verify(stub, times(1)).getIdentifiedObjects(any())

        assertThat(result.wasSuccessful, equalTo(false))
        expect { throw result.thrown }
            .toThrow(NoSuchElementException::class.java)
            .withMessage("No object with mRID f002 could be found.")

        validateFeederNetwork(service, NetworkService())
    }

    @Test
    internal fun `calls error handler when getting the feeder throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validFeeder = false)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validFeeder", true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validFeeder = false)

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validFeeder", false)
    }

    @Test
    internal fun `calls error handler when getting the feeder equipment throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validEquipment = false)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(1)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validEquipment", true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder equipment throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validEquipment = false)

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(1)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validEquipment", false)
    }


    @Test
    internal fun `calls error handler when getting the feeder substation throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validSubstation = false)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validSubstation", true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder substation throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validSubstation = false)

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validSubstation", false)
    }

    @Test
    internal fun `calls error handler when getting the feeder equipment connectivity throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validConnectivityNode = false)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(3)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validConnectivityNode", true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder equipment connectivity throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validConnectivityNode = false)

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(3)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validConnectivityNode", false)
    }

    @Test
    internal fun `calls error handler when getting the feeder equipment location throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validLocation = false)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validLocation", true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder equipment location throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validLocation = false)

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validLocation", false)
    }

    @Test
    internal fun `calls error handler when getting the feeder equipment wire info throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validWireInfo = false)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validWireInfo", true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder equipment wire info throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validWireInfo = false)

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validWireInfo", false)
    }

    @Test
    internal fun `calls error handler when getting the feeder equipment sequence info throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validPerLengthSequenceInformation = false)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validPerLengthSequenceInformation", true)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder equipment sequence info throws`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService, validPerLengthSequenceInformation = false)

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>(service, "f001")

        verify(stub, times(1)).getEquipmentForContainer(any())
        verify(stub, times(2)).getIdentifiedObjects(any())
        validateNestedFailure(result, "validPerLengthSequenceInformation", false)
    }

    @Test
    internal fun `getIdentifiedObjects returns failed mRID when an mRID is not found`() {
        val mRIDs = listOf("id1", "id2")
        val response = createResponse(NetworkIdentifiedObject.newBuilder(), NetworkIdentifiedObject.Builder::getAcLineSegmentBuilder, mRIDs[0])

        doReturn(listOf(response).iterator()).`when`(stub).getIdentifiedObjects(any())

        val result = consumerClient.getIdentifiedObjects(service, mRIDs)

        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.objects.size, equalTo(1))
        assertThat(result.value.objects["id1"], instanceOf(AcLineSegment::class.java))
        assertThat(result.value.failed, containsInAnyOrder(mRIDs[1]))

        verify(stub).getIdentifiedObjects(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build())
        clearInvocations(stub)
    }

    @Test
    internal fun `getIdentifiedObjects returns map containing existing entries in the service`() {
        val mRIDs = listOf("id1", "id2", "id3")
        val response2 = createResponse(NetworkIdentifiedObject.newBuilder(), NetworkIdentifiedObject.Builder::getAcLineSegmentBuilder, mRIDs[1])
        val response3 = createResponse(NetworkIdentifiedObject.newBuilder(), NetworkIdentifiedObject.Builder::getBreakerBuilder, mRIDs[2])
        val acls = AcLineSegment(mRIDs[0])
        service.add(acls)

        doReturn(listOf(response2, response3).iterator()).`when`(stub).getIdentifiedObjects(any())

        val result = consumerClient.getIdentifiedObjects(service, mRIDs)

        assertThat(result.value.objects, hasEntry("id1", acls))
        assertThat(result.value.objects, hasKey("id2"))
        assertThat(result.value.objects, hasKey("id3"))
        assertThat(result.value.objects.size, equalTo(3))
        assertThat(result.value.failed, empty())
    }

    @Test
    internal fun `getEquipmentForContainer returns equipment for a given container`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService)

        val ns = NetworkService()
        val result = consumerClient.getEquipmentForContainer(ns, "f001")

        assertThat(result.value.objects.size, equalTo(ns.num(Equipment::class)))
        assertThat(result.value.objects.size, equalTo(3))
        assertThat(ns.listOf(IdentifiedObject::class).map { it.mRID }, containsInAnyOrder("fsp", "c2", "tx"))
    }

    @Test
    internal fun `getCurrentEquipmentForFeeder returns equipment for a given Feeder`() {
        val expectedService = FeederNetworkWithCurrent.create()
        configureFeederResponses(expectedService)

        var ns = NetworkService()
        var result = consumerClient.getEquipmentForContainer(ns, "f001")
        assertThat(result.value.objects.size, equalTo(ns.num(Equipment::class)))
        assertThat(result.value.objects.size, equalTo(7))
        assertThat(ns.listOf(IdentifiedObject::class).map { it.mRID }, containsInAnyOrder("fsp", "c2", "tx", "c3", "sw", "c4", "tx2"))

        ns = NetworkService()
        result = consumerClient.getCurrentEquipmentForFeeder(ns, "f001")

        assertThat(result.value.objects.size, equalTo(ns.num(Equipment::class)))
        assertThat(result.value.objects.size, equalTo(5))
        assertThat(ns.listOf(IdentifiedObject::class).map { it.mRID }, containsInAnyOrder("fsp", "c2", "tx", "c3", "sw"))
    }

    @Test
    internal fun `getEquipmentForRestriction returns equipment for a given OperationalRestriction`() {
        val expectedService = OperationalRestrictionTestNetworks.create()
        configureResponses(expectedService)

        val ns = NetworkService()
        val result = consumerClient.getEquipmentForRestriction(ns, "or1").throwOnError()

        assertThat(result.value.objects.size, equalTo(ns.num(Equipment::class)))
        assertThat(result.value.objects.size, equalTo(3))
        assertThat(ns.listOf(IdentifiedObject::class).map { it.mRID }, containsInAnyOrder("fsp", "c2", "tx"))
    }

    @Test
    internal fun `getTerminalsForNode returns terminals for a given ConnectivityNode`() {
        val expectedService = ConnectivityNodeNetworks.createSimpleConnectivityNode()
        configureResponses(expectedService)

        val ns = NetworkService()
        val result = consumerClient.getTerminalsForConnectivityNode(ns, "cn1").throwOnError()

        assertThat(result.value.objects.size, equalTo(ns.num(Terminal::class)))
        assertThat(result.value.objects.size, equalTo(3))
        expectedService.get<ConnectivityNode>("cn1")!!.terminals.forEach {
            assertThat(ns[it.mRID], notNullValue())
        }
    }

    @Test
    internal fun `direct object variant coverage`() {
        val ns = NetworkService()
        val expectedResult = mock<GrpcResult<MultiObjectResult>>()

        val feeder = Feeder()
        val operationalRestriction = OperationalRestriction()
        val connectivityNode = ConnectivityNode()

        doReturn(expectedResult).`when`(consumerClient).getEquipmentForContainer(eq(ns), eq(feeder.mRID))
        doReturn(expectedResult).`when`(consumerClient).getEquipmentForRestriction(eq(ns), eq(operationalRestriction.mRID))
        doReturn(expectedResult).`when`(consumerClient).getCurrentEquipmentForFeeder(eq(ns), eq(feeder.mRID))
        doReturn(expectedResult).`when`(consumerClient).getTerminalsForConnectivityNode(eq(ns), eq(connectivityNode.mRID))

        assertThat(consumerClient.getEquipmentForContainer(ns, feeder), equalTo(expectedResult))
        assertThat(consumerClient.getEquipmentForRestriction(ns, operationalRestriction), equalTo(expectedResult))
        assertThat(consumerClient.getCurrentEquipmentForFeeder(ns, feeder), equalTo(expectedResult))
        assertThat(consumerClient.getTerminalsForConnectivityNode(ns, connectivityNode), equalTo(expectedResult))
    }

    @Test
    internal fun `can get a loop`() {
        val expectedService = LoopNetwork.create()
        configureResponses(expectedService)

        val mRID = "BTS-ZEP-BEN-BTS-CBR"
        val expectedContainers = listOf("BTS", "ZEP", "BEN", "CBR", "BTSZEP", "ZEPBENCBR", "BTSBEN")

        val result = consumerClient.getEquipmentForLoop(service, mRID)
        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.failed, empty())

        val identifiedObjectRequestCaptor = argumentCaptor<GetIdentifiedObjectsRequest>()
        val equipmentContainerRequestCaptor = argumentCaptor<GetEquipmentForContainerRequest>()

        verify(stub, atLeastOnce()).getIdentifiedObjects(identifiedObjectRequestCaptor.capture())
        verify(stub, times(expectedContainers.size)).getEquipmentForContainer(equipmentContainerRequestCaptor.capture())

        assertThat(identifiedObjectRequestCaptor.firstValue.mridsList[0], equalTo("BTS-ZEP-BEN-BTS-CBR"))
        assertThat(equipmentContainerRequestCaptor.allValues.map { it.mrid }, containsInAnyOrder(*expectedContainers.toTypedArray()))
    }

    @Test
    internal fun `can get all loops`() {
        val expectedService = LoopNetwork.create()
        configureResponses(expectedService)

        val expectedContainers = listOf(
            "TG", "ZTS", "BTS", "ZEP", "BEN", "CBR", "ACT",
            "TGZTS", "TGBTS", "ZTSBTS", "BTSZEP", "BTSBEN", "ZEPBENCBR", "BTSACT", "ZTSACT"
        )

        val result = consumerClient.getAllLoops(service)
        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.failed, empty())

        val equipmentContainerRequestCaptor = argumentCaptor<GetEquipmentForContainerRequest>()

        verify(stub).getNetworkHierarchy(any())
        verify(stub, times(expectedContainers.size)).getEquipmentForContainer(equipmentContainerRequestCaptor.capture())

        assertThat(equipmentContainerRequestCaptor.allValues.map { it.mrid }, containsInAnyOrder(*expectedContainers.toTypedArray()))
    }

    @Test
    internal fun `get equipment container validates type`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService)

        val result = consumerClient.getEquipmentContainer<Circuit>(service, "f001")

        assertThat(result.wasSuccessful, equalTo(false))
        expect { throw result.thrown }
            .toThrow(ClassCastException::class.java)
            .withMessage("Unable to extract Circuit network from ${expectedService.get<Feeder>("f001")?.typeNameAndMRID()}.")
    }

    private fun createResponse(
        identifiedObjectBuilder: NetworkIdentifiedObject.Builder,
        subClassBuilder: (NetworkIdentifiedObject.Builder) -> Any,
        mRID: String
    ): GetIdentifiedObjectsResponse {
        return createResponse(identifiedObjectBuilder, subClassBuilder(identifiedObjectBuilder), mRID)
    }

    private fun createResponse(
        identifiedObjectBuilder: NetworkIdentifiedObject.Builder,
        subClassBuilder: Any,
        mRID: String
    ): GetIdentifiedObjectsResponse {
        buildFromBuilder(subClassBuilder, mRID)
        println(identifiedObjectBuilder)

        val responseBuilder = GetIdentifiedObjectsResponse.newBuilder()

        responseBuilder.identifiedObject = identifiedObjectBuilder.build()

        return responseBuilder.build()
    }

    private fun isSupported(type: NetworkIdentifiedObject.IdentifiedObjectCase): Boolean =
        type != NetworkIdentifiedObject.IdentifiedObjectCase.OTHER

    private fun validateNetworkHierarchy(actual: NetworkHierarchy?, expected: NetworkHierarchy) {
        assertThat(actual, notNullValue())

        validateMap(actual!!.geographicalRegions, expected.geographicalRegions)
        validateMap(actual.subGeographicalRegions, expected.subGeographicalRegions)
        validateMap(actual.substations, expected.substations)
        validateMap(actual.feeders, expected.feeders)
        validateMap(actual.circuits, expected.circuits)
        validateMap(actual.loops, expected.loops)
    }

    private fun <T : IdentifiedObject> validateMap(actualMap: Map<String, T>, expectedMap: Map<String, T>) {
        assertThat(actualMap.size, equalTo(expectedMap.size))

        actualMap.forEach { (mRID, it) ->
            val expected = expectedMap[mRID]
            assertThat(expected, notNullValue())

            assertThat(NetworkServiceComparator().compare(it, expected!!).differences, anEmptyMap())
        }
    }

    private fun configureFeederResponses(
        expectedService: NetworkService,
        validFeeder: Boolean = true,
        validSubstation: Boolean = true,
        validEquipment: Boolean = true,
        validConnectivityNode: Boolean = true,
        validLocation: Boolean = true,
        validWireInfo: Boolean = true,
        validPerLengthSequenceInformation: Boolean = true
    ) {
        doAnswer {
            val request = it.getArgument<GetIdentifiedObjectsRequest>(0)
            val objects = mutableListOf<IdentifiedObject>()
            request.mridsList.forEach { mRID ->
                expectedService.get<IdentifiedObject>(mRID)?.let { identifiedObject ->
                    if ((identifiedObject is Feeder) && !validFeeder)
                        throw throw Exception("validFeeder")
                    else if ((identifiedObject is Substation) && !validSubstation)
                        throw throw Exception("validSubstation")
                    else if ((identifiedObject is Equipment) && !validEquipment)
                        throw throw Exception("validEquipment")
                    else if ((identifiedObject is ConnectivityNode) && !validConnectivityNode)
                        throw throw Exception("validConnectivityNode")
                    else if ((identifiedObject is Location) && !validLocation)
                        throw throw Exception("validLocation")
                    else if ((identifiedObject is WireInfo) && !validWireInfo)
                        throw throw Exception("validWireInfo")
                    else if ((identifiedObject is PerLengthSequenceImpedance) && !validPerLengthSequenceInformation)
                        throw throw Exception("validPerLengthSequenceInformation")
                    else
                        objects.add(identifiedObject)
                }
            }
            responseOf(objects)
        }
            .`when`(stub)
            .getIdentifiedObjects(any())

        doAnswer {
            val request = it.getArgument<GetEquipmentForContainerRequest>(0)
            val objects = mutableListOf<IdentifiedObject>()
            val feeder = expectedService.get<Feeder>(request.mrid)!!
            if (!validEquipment)
                throw throw Exception("validEquipment")
            feeder.equipment.forEach { equip -> objects.add(equip) }
            containerEquipmentResponseOf(objects)
        }
            .`when`(stub)
            .getEquipmentForContainer(any())

        doAnswer {
            val request = it.getArgument<GetCurrentEquipmentForFeederRequest>(0)
            val objects = mutableListOf<IdentifiedObject>()
            val feeder = expectedService.get<Feeder>(request.mrid)!!
            if (!validEquipment)
                throw throw Exception("validEquipment")
            feeder.currentEquipment.forEach { equip -> objects.add(equip) }
            currentEquipmentResponseOf(objects)
        }
            .`when`(stub)
            .getCurrentEquipmentForFeeder(any())
    }

    private fun configureResponses(expectedService: NetworkService) {
        doAnswer {
            val request = it.getArgument<GetEquipmentForRestrictionRequest>(0)
            restrictionEquipmentResponseOf(expectedService.get<OperationalRestriction>(request.mrid)!!.equipment.toList())
        }
            .`when`(stub)
            .getEquipmentForRestriction(any())

        doAnswer { inv ->
            val request = inv.getArgument<GetTerminalsForNodeRequest>(0)
            nodeTerminalResponseOf(expectedService.get<ConnectivityNode>(request.mrid)!!.terminals.toList())
        }
            .`when`(stub)
            .getTerminalsForNode(any())

        doAnswer { inv ->
            val request = inv.getArgument<GetIdentifiedObjectsRequest>(0)
            responseOf(request.mridsList.map { expectedService[it]!! })
        }.`when`(stub)
            .getIdentifiedObjects(any())

        doAnswer {
            val request = it.getArgument<GetEquipmentForContainerRequest>(0)
            containerEquipmentResponseOf(expectedService.get<EquipmentContainer>(request.mrid)!!.equipment.toList())
        }.`when`(stub)
            .getEquipmentForContainer(any())

        doReturn(
            networkHierarchyResponseOf(
                expectedService.listOf(),
                expectedService.listOf(),
                expectedService.listOf(),
                expectedService.listOf(),
                expectedService.listOf(),
                expectedService.listOf()
            )
        ).`when`(stub)
            .getNetworkHierarchy(any())
    }

    private fun responseOf(objects: List<IdentifiedObject>): MutableIterator<GetIdentifiedObjectsResponse> {
        val responses = mutableListOf<GetIdentifiedObjectsResponse>()
        objects.forEach {
            val response = GetIdentifiedObjectsResponse.newBuilder()
            buildNetworkIdentifiedObject(it, response.identifiedObjectBuilder)
            responses.add(response.build())
        }
        return responses.iterator()
    }

    private fun restrictionEquipmentResponseOf(objects: List<IdentifiedObject>): MutableIterator<GetEquipmentForRestrictionResponse> {
        val responses = mutableListOf<GetEquipmentForRestrictionResponse>()
        objects.forEach {
            val response = GetEquipmentForRestrictionResponse.newBuilder()
            buildNetworkIdentifiedObject(it, response.identifiedObjectBuilder)
            responses.add(response.build())
        }
        return responses.iterator()
    }

    private fun containerEquipmentResponseOf(objects: List<IdentifiedObject>): MutableIterator<GetEquipmentForContainerResponse> {
        val responses = mutableListOf<GetEquipmentForContainerResponse>()
        objects.forEach {
            val response = GetEquipmentForContainerResponse.newBuilder()
            buildNetworkIdentifiedObject(it, response.identifiedObjectBuilder)
            responses.add(response.build())
        }
        return responses.iterator()
    }

    private fun currentEquipmentResponseOf(objects: List<IdentifiedObject>): MutableIterator<GetCurrentEquipmentForFeederResponse> {
        val responses = mutableListOf<GetCurrentEquipmentForFeederResponse>()
        objects.forEach {
            val response = GetCurrentEquipmentForFeederResponse.newBuilder()
            buildNetworkIdentifiedObject(it, response.identifiedObjectBuilder)
            responses.add(response.build())
        }
        return responses.iterator()
    }

    private fun nodeTerminalResponseOf(objects: List<Terminal>): MutableIterator<GetTerminalsForNodeResponse> {
        val responses = mutableListOf<GetTerminalsForNodeResponse>()
        objects.forEach {
            responses.add(GetTerminalsForNodeResponse.newBuilder().setTerminal(it.toPb()).build())
        }
        return responses.iterator()
    }

    private fun networkHierarchyResponseOf(
        geographicalRegions: List<GeographicalRegion>,
        subGeographicalRegions: List<SubGeographicalRegion>,
        substations: List<Substation>,
        feeders: List<Feeder>,
        circuits: List<Circuit>,
        loops: List<Loop>
    ): GetNetworkHierarchyResponse = GetNetworkHierarchyResponse.newBuilder()
        .addAllGeographicalRegions(geographicalRegions.map { it.toPb() })
        .addAllSubGeographicalRegions(subGeographicalRegions.map { it.toPb() })
        .addAllSubstations(substations.map { it.toPb() })
        .addAllFeeders(feeders.map { it.toPb() })
        .addAllCircuits(circuits.map { it.toPb() })
        .addAllLoops(loops.map { it.toPb() })
        .build()

    private fun buildNetworkIdentifiedObject(obj: IdentifiedObject, identifiedObjectBuilder: NetworkIdentifiedObject.Builder): NetworkIdentifiedObject? {
        when (obj) {
            is CableInfo -> identifiedObjectBuilder.cableInfo = obj.toPb()
            is AcLineSegment -> identifiedObjectBuilder.acLineSegment = obj.toPb()
            is Breaker -> identifiedObjectBuilder.breaker = obj.toPb()
            is EnergySource -> identifiedObjectBuilder.energySource = obj.toPb()
            is Junction -> identifiedObjectBuilder.junction = obj.toPb()
            is PowerTransformer -> identifiedObjectBuilder.powerTransformer = obj.toPb()
            is ConnectivityNode -> identifiedObjectBuilder.connectivityNode = obj.toPb()
            is EnergySourcePhase -> identifiedObjectBuilder.energySourcePhase = obj.toPb()
            is Feeder -> identifiedObjectBuilder.feeder = obj.toPb()
            is Location -> identifiedObjectBuilder.location = obj.toPb()
            is OverheadWireInfo -> identifiedObjectBuilder.overheadWireInfo = obj.toPb()
            is PerLengthSequenceImpedance -> identifiedObjectBuilder.perLengthSequenceImpedance = obj.toPb()
            is PowerTransformerEnd -> identifiedObjectBuilder.powerTransformerEnd = obj.toPb()
            is Substation -> identifiedObjectBuilder.substation = obj.toPb()
            is Terminal -> identifiedObjectBuilder.terminal = obj.toPb()
            is Loop -> identifiedObjectBuilder.loop = obj.toPb()
            is Circuit -> identifiedObjectBuilder.circuit = obj.toPb()
            is BaseVoltage -> identifiedObjectBuilder.baseVoltage = obj.toPb()
            else -> throw Exception("Missing class in create response: ${obj.typeNameAndMRID()}")
        }

        return identifiedObjectBuilder.build()
    }

    private fun validateFeederNetwork(actual: NetworkService?, expectedService: NetworkService) {
        assertThat(actual, notNullValue())
        val differences = NetworkServiceComparator().compare(actual!!, expectedService)

        println(differences)

        assertThat("missing from source", differences.missingFromSource(), empty())
        assertThat("missing from target", differences.missingFromTarget(), empty())
        assertThat("has differences", differences.modifications().entries, empty())
    }

    private fun validateNestedFailure(result: GrpcResult<*>, expectedMessage: String, expectHandled: Boolean) {
        assertThat(result.wasFailure, equalTo(true))
        assertThat(result.thrown.message, equalTo(expectedMessage))
        assertThat(result.wasErrorHandled, equalTo(expectHandled))
        assertThat(onErrorHandler.lastError, if (expectHandled) notNullValue() else nullValue())
    }

}
