/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.streaming.get

import org.mockito.kotlin.*
import com.zepben.evolve.cim.iec61968.assetinfo.CableInfo
import com.zepben.evolve.cim.iec61968.assetinfo.OverheadWireInfo
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
import com.zepben.evolve.streaming.get.testservices.TestNetworkConsumerService
import com.zepben.evolve.streaming.grpc.CaptureLastRpcErrorHandler
import com.zepben.evolve.streaming.grpc.GrpcResult
import com.zepben.protobuf.nc.*
import com.zepben.testutils.exception.ExpectException.expect
import com.zepben.testutils.junit.SystemLogExtension
import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.testing.GrpcCleanupRule
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import com.zepben.protobuf.nc.NetworkIdentifiedObject as NIO


internal class NetworkConsumerClientTest {

    @JvmField
    @RegisterExtension
    var systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    @JvmField
    @Rule
    val grpcCleanup: GrpcCleanupRule = GrpcCleanupRule()

    private val serverName = InProcessServerBuilder.generateName()

    private val consumerService = TestNetworkConsumerService()

    private val channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build())
    private val onErrorHandler = CaptureLastRpcErrorHandler()
    private val consumerClient = spy(NetworkConsumerClient(channel).apply { addErrorHandler(onErrorHandler) })
    private val service = consumerClient.service

    private val serverException = IllegalStateException("custom message")

    @BeforeEach
    internal fun beforeEach() {
        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(consumerService).build().start())
    }

    @Test
    internal fun `can get all supported types`() {
        var counter = 0
        val builder = NIO.newBuilder()

        forEachBuilder(builder) {
            val mRID = "id" + ++counter
            val response = createResponse(builder, it, mRID)

            consumerService.onGetIdentifiedObjects = spy { request, resp ->
                assertThat(request.mridsList, containsInAnyOrder(mRID))
                resp.onNext(response)
            }

            val result = consumerClient.getIdentifiedObject(mRID)

            response.identifiedObjectsList.forEach { nio ->
                val type = nio.identifiedObjectCase
                if (isSupported(type)) {
                    assertThat(result.wasSuccessful, equalTo(true))
                    assertThat(result.value.mRID, equalTo(mRID))
                } else {
                    assertThat(result.wasFailure, equalTo(true))
                    assertThat(result.thrown, instanceOf(StatusRuntimeException::class.java))
                    assertThat(result.thrown.cause, instanceOf(UnsupportedOperationException::class.java))
                    assertThat(result.thrown.cause?.message, equalTo("Identified object type $type is not supported by the network service"))
                    assertThat(result.thrown, equalTo(onErrorHandler.lastError))
                }
            }

            verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build()), any())
        }
    }

    @Test
    internal fun `returns error when object is not found`() {
        val mRID = "unknown"
        consumerService.onGetIdentifiedObjects = spy { _, _ -> }

        val result = consumerClient.getIdentifiedObject(mRID)

        verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build()), any())
        assertThat(result.wasFailure, equalTo(true))
        expect { throw result.thrown }
            .toThrow(NoSuchElementException::class.java)
            .withMessage("No object with mRID $mRID could be found.")
    }

    @Test
    internal fun `calls error handler when getting an IdentifiedObject throws`() {
        val mRID = "1234"
        consumerService.onGetIdentifiedObjects = spy { _, _ -> throw serverException }

        val result = consumerClient.getIdentifiedObject(mRID)

        verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build()), any())
        validateFailure(onErrorHandler, result, serverException)
    }


    @Test
    internal fun `captures unhandled exceptions when getting an IdentifiedObject throws`() {
        val mRID = "1234"
        consumerService.onGetIdentifiedObjects = spy { _, _ -> throw serverException }

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getIdentifiedObject(mRID)

        verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addMrids(mRID).build()), any())
        validateFailure(onErrorHandler, result, serverException, expectHandled = false)
    }

    @Test
    internal fun `can get multiple identified objects in single call`() {
        val mRIDs = listOf("id1", "id2", "id3")

        consumerService.onGetIdentifiedObjects = spy { _, response ->
            response.onNext(createResponse(NIO.newBuilder(), NIO.Builder::getAcLineSegmentBuilder, mRIDs[0]))
            response.onNext(createResponse(NIO.newBuilder(), NIO.Builder::getAcLineSegmentBuilder, mRIDs[1]))
            response.onNext(createResponse(NIO.newBuilder(), NIO.Builder::getBreakerBuilder, mRIDs[2]))
        }

        val result = consumerClient.getIdentifiedObjects(mRIDs.asSequence())

        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.objects.size, equalTo(3))
        assertThat(result.value.objects[mRIDs[0]], instanceOf(AcLineSegment::class.java))
        assertThat(result.value.objects[mRIDs[1]], instanceOf(AcLineSegment::class.java))
        assertThat(result.value.objects[mRIDs[2]], instanceOf(Breaker::class.java))

        verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build()), any())
    }

    private fun createResponse(
        identifiedObjectBuilder: NIO.Builder,
        subClassBuilder: Any,
        mRID: String
    ): GetIdentifiedObjectsResponse {
        buildFromBuilder(subClassBuilder, mRID)
        println(identifiedObjectBuilder)

        val responseBuilder = GetIdentifiedObjectsResponse.newBuilder()

        responseBuilder.addIdentifiedObjects(identifiedObjectBuilder.build())

        return responseBuilder.build()
    }

    private fun isSupported(type: NIO.IdentifiedObjectCase): Boolean =
        type != NIO.IdentifiedObjectCase.OTHER

    @Test
    internal fun `calls error handler when getting multiple IdentifiedObject throws`() {
        val mRIDs = listOf("id1", "id2", "id3")
        consumerService.onGetIdentifiedObjects = spy { _, _ -> throw serverException }

        val result = consumerClient.getIdentifiedObjects(mRIDs.asSequence())

        verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build()), any())
        validateFailure(onErrorHandler, result, serverException)
    }

    @Test
    internal fun `captures unhandled exceptions when getting multiple IdentifiedObject throws`() {
        val mRIDs = listOf("id1", "id2", "id3")
        consumerService.onGetIdentifiedObjects = spy { _, _ -> throw serverException }

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getIdentifiedObjects(mRIDs)

        verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build()), any())
        validateFailure(onErrorHandler, result, serverException, expectHandled = false)
    }

    @Test
    internal fun `can get network hierarchy`() {
        consumerService.onGetNetworkHierarchy = spy { _, response -> response.onNext(NetworkHierarchyAllTypes.createResponse()) }

        val result = consumerClient.getNetworkHierarchy()

        verify(consumerService.onGetNetworkHierarchy).invoke(eq(GetNetworkHierarchyRequest.newBuilder().build()), any())
        assertThat(result.wasSuccessful, equalTo(true))
        validateNetworkHierarchy(result.value, NetworkHierarchyAllTypes.createNetworkHierarchy())
    }

    @Test
    internal fun `calls error handler when getting the network hierarchy throws`() {
        consumerService.onGetNetworkHierarchy = spy { _, _ -> throw serverException }

        val result = consumerClient.getNetworkHierarchy()

        verify(consumerService.onGetNetworkHierarchy).invoke(eq(GetNetworkHierarchyRequest.newBuilder().build()), any())
        validateFailure(onErrorHandler, result, serverException)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the network hierarchy throws`() {
        consumerService.onGetNetworkHierarchy = spy { _, _ -> throw serverException }

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getNetworkHierarchy()

        verify(consumerService.onGetNetworkHierarchy).invoke(eq(GetNetworkHierarchyRequest.newBuilder().build()), any())
        validateFailure(onErrorHandler, result, serverException, expectHandled = false)
    }

    @Test
    internal fun `can get feeder`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService)

        val mRID = "f001"
        val result = consumerClient.getEquipmentContainer<Feeder>(mRID)

        verify(consumerService.onGetNetworkHierarchy, times(1)).invoke(any(), any())
        verify(consumerService.onGetEquipmentForContainers, times(1)).invoke(any(), any())
        verify(consumerService.onGetIdentifiedObjects, times(3)).invoke(any(), any())
        verifyNoMoreInteractions(consumerService.onGetNetworkHierarchy)

        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.objects.containsKey(mRID), equalTo(true))
        assertThat(result.value.objects.size, equalTo(20))

        result.value.objects.values.forEach { assertThat(service[it.mRID], equalTo(it)) }
        service.sequenceOf<IdentifiedObject>().forEach {
            // Hierarchy objects are not added to the result collection.
            if ((it.mRID != mRID) && ((it is GeographicalRegion) || (it is SubGeographicalRegion) || (it is Substation) || (it is Feeder) || (it is Circuit) || (it is Loop)))
                assertThat(result.value.objects[it.mRID], nullValue())
            else
                assertThat(result.value.objects[it.mRID], equalTo(it))
        }
        assertThat(result.value.failed, empty())

        val actualFeeder: Feeder = service[mRID]!!
        val expectedFeeder: Feeder = expectedService[mRID]!!

        assertThat(NetworkServiceComparator().compare(actualFeeder, expectedFeeder).differences, anEmptyMap())
    }

    @Test
    internal fun `handles missing feeder`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService)

        val result = consumerClient.getEquipmentContainer<Feeder>("f002")

        verify(consumerService.onGetIdentifiedObjects, times(1)).invoke(any(), any())

        assertThat(result.wasSuccessful, equalTo(false))
        expect { throw result.thrown }
            .toThrow(NoSuchElementException::class.java)
            .withMessage("No object with mRID f002 could be found.")

        validateFeederNetwork(service, NetworkHierarchyAllTypes.createService())
    }

    @Test
    internal fun `calls error handler when getting the feeder throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, Feeder::class.java)!!

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetIdentifiedObjects, times(1)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, Feeder::class.java)!!

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetIdentifiedObjects, times(1)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException, expectHandled = false)
    }

    @Test
    internal fun `calls error handler when getting the feeder equipment throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, Equipment::class.java)!!

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetEquipmentForContainers, times(1)).invoke(any(), any())
        verify(consumerService.onGetIdentifiedObjects, times(1)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder equipment throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, Equipment::class.java)!!

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetEquipmentForContainers, times(1)).invoke(any(), any())
        verify(consumerService.onGetIdentifiedObjects, times(1)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException, expectHandled = false)
    }

    @Test
    internal fun `calls error handler when getting the feeder terminal throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, Terminal::class.java)!!

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetEquipmentForContainers, times(1)).invoke(any(), any())
        verify(consumerService.onGetIdentifiedObjects, times(2)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder terminal throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, Terminal::class.java)!!

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetEquipmentForContainers, times(1)).invoke(any(), any())
        verify(consumerService.onGetIdentifiedObjects, times(2)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException, expectHandled = false)
    }

    @Test
    internal fun `calls error handler when getting the feeder equipment connectivity throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, ConnectivityNode::class.java)!!

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetIdentifiedObjects, times(3)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException)
    }

    @Test
    internal fun `captures unhandled exceptions when getting the feeder equipment connectivity throws`() {
        val expectedService = FeederNetwork.create()
        val expectedException = configureFeederResponses(expectedService, ConnectivityNode::class.java)!!

        consumerClient.removeErrorHandler(onErrorHandler)

        val result = consumerClient.getEquipmentContainer<Feeder>("f001")

        verify(consumerService.onGetIdentifiedObjects, times(3)).invoke(any(), any())
        validateFailure(onErrorHandler, result, expectedException, expectHandled = false)
    }

    @Test
    internal fun `getIdentifiedObjects returns failed mRID when an mRID is not found`() {
        val mRIDs = listOf("id1", "id2")

        consumerService.onGetIdentifiedObjects = spy { _, response ->
            response.onNext(createResponse(NIO.newBuilder(), NIO.Builder::getAcLineSegmentBuilder, mRIDs[0]))
        }

        val result = consumerClient.getIdentifiedObjects(mRIDs)

        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.objects.size, equalTo(1))
        assertThat(result.value.objects["id1"], instanceOf(AcLineSegment::class.java))
        assertThat(result.value.failed, containsInAnyOrder(mRIDs[1]))

        verify(consumerService.onGetIdentifiedObjects).invoke(eq(GetIdentifiedObjectsRequest.newBuilder().addAllMrids(mRIDs).build()), any())
    }

    @Test
    internal fun `getIdentifiedObjects returns map containing existing entries in the service`() {
        val mRIDs = listOf("id1", "id2", "id3")
        val acls = AcLineSegment(mRIDs[0])
        service.add(acls)

        consumerService.onGetIdentifiedObjects = spy { _, response ->
            response.onNext(createResponse(NIO.newBuilder(), NIO.Builder::getAcLineSegmentBuilder, mRIDs[0]))
            response.onNext(createResponse(NIO.newBuilder(), NIO.Builder::getAcLineSegmentBuilder, mRIDs[1]))
            response.onNext(createResponse(NIO.newBuilder(), NIO.Builder::getBreakerBuilder, mRIDs[2]))
        }

        val result = consumerClient.getIdentifiedObjects(mRIDs)

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

        val result = consumerClient.getEquipmentForContainer("f001")

        assertThat(result.value.objects.size, equalTo(service.num(Equipment::class)))
        assertThat(result.value.objects.size, equalTo(3))
        assertThat(service.listOf(IdentifiedObject::class).map { it.mRID }, containsInAnyOrder("fsp", "c2", "tx"))
    }

    @Test
    internal fun `getCurrentEquipmentForFeeder returns equipment for a given Feeder`() {
        val expectedService = FeederNetworkWithCurrent.create()
        configureFeederResponses(expectedService)

        val result = consumerClient.getCurrentEquipmentForFeeder("f001")

        assertThat(result.value.objects.size, equalTo(service.num(Equipment::class)))
        assertThat(result.value.objects.size, equalTo(5))
        assertThat(service.listOf(IdentifiedObject::class).map { it.mRID }, containsInAnyOrder("fsp", "c2", "tx", "c3", "sw"))
    }

    @Test
    internal fun `getEquipmentForRestriction returns equipment for a given OperationalRestriction`() {
        val expectedService = OperationalRestrictionTestNetworks.create()
        configureResponses(expectedService)

        val result = consumerClient.getEquipmentForRestriction("or1").throwOnError()

        assertThat(result.value.objects.size, equalTo(service.num(Equipment::class)))
        assertThat(result.value.objects.size, equalTo(3))
        assertThat(service.listOf(IdentifiedObject::class).map { it.mRID }, containsInAnyOrder("fsp", "c2", "tx"))
    }

    @Test
    internal fun `getTerminalsForNode returns terminals for a given ConnectivityNode`() {
        val expectedService = ConnectivityNodeNetworks.createSimpleConnectivityNode()
        configureResponses(expectedService)

        val result = consumerClient.getTerminalsForConnectivityNode("cn1").throwOnError()

        assertThat(result.value.objects.size, equalTo(service.num(Terminal::class)))
        assertThat(result.value.objects.size, equalTo(3))
        expectedService.get<ConnectivityNode>("cn1")!!.terminals.forEach {
            assertThat(service[it.mRID], notNullValue())
        }
    }

    @Test
    internal fun `direct object variant coverage`() {
        val expectedResult = mock<GrpcResult<MultiObjectResult>>()

        val feeder = Feeder()
        val operationalRestriction = OperationalRestriction()
        val connectivityNode = ConnectivityNode()

        doReturn(expectedResult).`when`(consumerClient).getEquipmentForContainer(eq(feeder.mRID))
        doReturn(expectedResult).`when`(consumerClient).getEquipmentForRestriction(eq(operationalRestriction.mRID))
        doReturn(expectedResult).`when`(consumerClient).getCurrentEquipmentForFeeder(eq(feeder.mRID))
        doReturn(expectedResult).`when`(consumerClient).getTerminalsForConnectivityNode(eq(connectivityNode.mRID))

        assertThat(consumerClient.getEquipmentForContainer(feeder), equalTo(expectedResult))
        assertThat(consumerClient.getEquipmentForRestriction(operationalRestriction), equalTo(expectedResult))
        assertThat(consumerClient.getCurrentEquipmentForFeeder(feeder), equalTo(expectedResult))
        assertThat(consumerClient.getTerminalsForConnectivityNode(connectivityNode), equalTo(expectedResult))
    }

    @Test
    internal fun `can get a loop`() {
        val loopNetwork = LoopNetwork.create()
        configureResponses(loopNetwork)

        val loop = "BTS-ZEP-BEN-BTS-CBR"
        val loopContainers = listOf("BTS", "ZEP", "BEN", "CBR", "BTSZEP", "ZEPBENCBR", "BTSBEN")
        val hierarchyObjs = listOf(
            "TG", "ZTS", "ACT",
            "TGZTS", "TGBTS", "ZTSBTS", "BTSACT", "ZTSACT",
            "TG-ZTS-BTS-TG", "ZTS-ACT-BTS",
            "ZEP001", "BEN001", "CBR001", "ACT001"
        )
        val containerEquip = listOf(
            "BTS-j-132000", "BTS-j-66000", "ZEP-j-66000", "ZEP-j-11000", "BEN-j-66000", "BEN-j-11000", "CBR-j-66000", "CBR-j-11000",
            "BTSZEP-j", "ZEPBENCBR-j", "BTSBEN-j"
        )
        val assocObjs = containerEquip.map { "$it-t" } + listOf("bv132", "bv66", "bv11")

        val mor = consumerClient.getEquipmentForLoop(loop).throwOnError().value
        assertThat(service.num<IdentifiedObject>(), equalTo((listOf(loop) + loopContainers + hierarchyObjs + containerEquip + assocObjs).size))
        assertThat(mor.objects.size, equalTo((listOf(loop) + loopContainers + containerEquip + assocObjs).size))
    }

    @Test
    internal fun `can get all loops`() {
        val expectedService = LoopNetwork.create()
        configureResponses(expectedService)

        val expectedContainers = listOf(
            "TG", "ZTS", "BTS", "ZEP", "BEN", "CBR", "ACT",
            "TGZTS", "TGBTS", "ZTSBTS", "BTSZEP", "BTSBEN", "ZEPBENCBR", "BTSACT", "ZTSACT"
        )

        val result = consumerClient.getAllLoops()
        assertThat(result.wasSuccessful, equalTo(true))
        assertThat(result.value.failed, empty())

        val equipmentContainersRequestCaptor = argumentCaptor<GetEquipmentForContainersRequest>()

        verify(consumerService.onGetNetworkHierarchy).invoke(any(), any())
        verify(consumerService.onGetEquipmentForContainers).invoke(equipmentContainersRequestCaptor.capture(), any())

        assertThat(equipmentContainersRequestCaptor.allValues.flatMap { it.mridsList }, containsInAnyOrder(*expectedContainers.toTypedArray()))
    }

    @Test
    internal fun `get equipment container validates type`() {
        val expectedService = FeederNetwork.create()
        configureFeederResponses(expectedService)

        val result = consumerClient.getEquipmentContainer<Circuit>("f001")

        assertThat(result.wasSuccessful, equalTo(false))
        expect { throw result.thrown }
            .toThrow(ClassCastException::class.java)
            .withMessage("Unable to extract Circuit networks from [${expectedService.get<Feeder>("f001")?.typeNameAndMRID()}].")
    }

    private fun createResponse(
        identifiedObjectBuilder: NIO.Builder,
        subClassBuilder: (NIO.Builder) -> Any,
        mRID: String
    ): GetIdentifiedObjectsResponse {
        return createResponse(identifiedObjectBuilder, subClassBuilder(identifiedObjectBuilder), mRID)
    }

    private fun validateNetworkHierarchy(actual: NetworkHierarchy, expected: NetworkHierarchy) {
        validateMap(actual.geographicalRegions, expected.geographicalRegions)
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

    private fun configureFeederResponses(expectedService: NetworkService, invalidObject: Class<out IdentifiedObject>? = null): Throwable? {
        val expectedException = createException(invalidObject)

        consumerService.onGetNetworkHierarchy = spy { _, response -> response.onNext(NetworkHierarchyAllTypes.createResponse()) }

        consumerService.onGetIdentifiedObjects = spy { request, response ->
            val objects = mutableListOf<IdentifiedObject>()
            request.mridsList.forEach { mRID ->
                expectedService.get<IdentifiedObject>(mRID)?.let { identifiedObject ->
                    if (identifiedObject::class.java == invalidObject)
                        throw expectedException!!
                    else
                        objects.add(identifiedObject)
                }
            }
            responseOf(objects).forEach { response.onNext(it) }
        }

        consumerService.onGetEquipmentForContainers = spy { request, response ->
            if (invalidObject == Equipment::class.java)
                throw expectedException!!

            containerEquipmentResponseOf(request.mridsList.flatMap { expectedService.get<EquipmentContainer>(it)!!.equipment }.distinct().toList())
                .forEach { response.onNext(it) }
        }

        consumerService.onGetCurrentEquipmentForFeeder = spy { request, response ->
            val objects = mutableListOf<IdentifiedObject>()
            val feeder = expectedService.get<Feeder>(request.mrid)!!
            if (invalidObject == Equipment::class.java)
                throw expectedException!!
            feeder.currentEquipment.forEach { equip -> objects.add(equip) }
            currentEquipmentResponseOf(objects).forEach { response.onNext(it) }
        }

        return expectedException
    }

    private fun createException(invalidObject: Class<out IdentifiedObject>? = null): Throwable? = when {
        invalidObject != null -> Exception("invalid ${invalidObject.simpleName}")
        else -> null
    }

    private fun configureResponses(expectedService: NetworkService) {
        consumerService.onGetEquipmentForRestriction = spy { request, response ->
            restrictionEquipmentResponseOf(expectedService.get<OperationalRestriction>(request.mrid)!!.equipment.toList()).forEach { response.onNext(it) }
        }

        consumerService.onGetTerminalsForNode = spy { request, response ->
            nodeTerminalResponseOf(expectedService.get<ConnectivityNode>(request.mrid)!!.terminals.toList()).forEach { response.onNext((it)) }
        }

        consumerService.onGetIdentifiedObjects = spy { request, response ->
            responseOf(request.mridsList.map { expectedService[it]!! }).forEach { response.onNext((it)) }
        }

        consumerService.onGetEquipmentForContainers = spy { request, response ->
            containerEquipmentResponseOf(request.mridsList.flatMap { expectedService.get<EquipmentContainer>(it)!!.equipment }.distinct().toList())
                .forEach { response.onNext((it)) }
        }

        consumerService.onGetNetworkHierarchy = spy { _, response ->
            response.onNext(
                networkHierarchyResponseOf(
                    expectedService.listOf(),
                    expectedService.listOf(),
                    expectedService.listOf(),
                    expectedService.listOf(),
                    expectedService.listOf(),
                    expectedService.listOf()
                )
            )
        }
    }


    private fun responseOf(objects: List<IdentifiedObject>): MutableIterator<GetIdentifiedObjectsResponse> {
        val responses = mutableListOf<GetIdentifiedObjectsResponse>()
        objects.forEach {
            responses.add(GetIdentifiedObjectsResponse.newBuilder().apply { buildNIO(it, addIdentifiedObjectsBuilder()) }.build())
        }
        return responses.iterator()
    }

    private fun restrictionEquipmentResponseOf(objects: List<IdentifiedObject>): MutableIterator<GetEquipmentForRestrictionResponse> {
        val responses = mutableListOf<GetEquipmentForRestrictionResponse>()
        objects.forEach {
            responses.add(GetEquipmentForRestrictionResponse.newBuilder().apply { buildNIO(it, addIdentifiedObjectsBuilder()) }.build())
        }
        return responses.iterator()
    }

    private fun containerEquipmentResponseOf(objects: List<IdentifiedObject>): MutableIterator<GetEquipmentForContainersResponse> {
        val responses = mutableListOf<GetEquipmentForContainersResponse>()
        objects.forEach {
            responses.add(GetEquipmentForContainersResponse.newBuilder().apply { buildNIO(it, addIdentifiedObjectsBuilder()) }.build())
        }
        return responses.iterator()
    }

    private fun currentEquipmentResponseOf(objects: List<IdentifiedObject>): MutableIterator<GetCurrentEquipmentForFeederResponse> {
        val responses = mutableListOf<GetCurrentEquipmentForFeederResponse>()
        objects.forEach {
            responses.add(GetCurrentEquipmentForFeederResponse.newBuilder().apply { buildNIO(it, addIdentifiedObjectsBuilder()) }.build())
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

    private fun buildNIO(obj: IdentifiedObject, identifiedObjectBuilder: NIO.Builder): NIO? {
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

}
