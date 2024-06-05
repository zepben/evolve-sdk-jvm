/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.streaming.grpc

import com.zepben.auth.client.ZepbenTokenFetcher
import com.zepben.auth.common.AuthMethod
import com.zepben.evolve.services.common.BaseService
import com.zepben.evolve.services.common.meta.ServiceInfo
import com.zepben.evolve.services.common.translator.BaseProtoToCim
import com.zepben.evolve.streaming.get.CimConsumerClient
import com.zepben.evolve.streaming.get.CustomerConsumerClient
import com.zepben.evolve.streaming.get.DiagramConsumerClient
import com.zepben.evolve.streaming.get.NetworkConsumerClient
import com.zepben.testutils.exception.ExceptionMatcher
import com.zepben.testutils.exception.ExpectException.Companion.expect
import com.zepben.testutils.exception.ExpectExceptionError
import io.grpc.*
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.mockk.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.KClass

internal class GrpcChannelBuilderTest {

    @AfterEach
    internal fun teardownMockks() {
        unmockkAll()
    }

    @Test
    internal fun `test max inbound message size is set`() {
        val insecureChannel = mockk<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234).usePlaintext().maxInboundMessageSize(any()).build() } returns insecureChannel

        var grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234)
            .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 2000))

        verify {
            NettyChannelBuilder.forAddress("hostname", 1234).usePlaintext().maxInboundMessageSize(2000)
        }
        assertThat(grpcChannel.channel, equalTo(insecureChannel))

        grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234)
            .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = DEFAULT_BUILD_ARGS.maxInboundMessageSize))

        verify {
            NettyChannelBuilder.forAddress("hostname", 1234).usePlaintext().maxInboundMessageSize(20971520)
        }
        assertThat(grpcChannel.channel, equalTo(insecureChannel))

        grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234)
            .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 2000))

        verify {
            NettyChannelBuilder.forAddress("hostname", 1234).usePlaintext().maxInboundMessageSize(2000)
        }
        assertThat(grpcChannel.channel, equalTo(insecureChannel))
    }

    @Test
    internal fun defaultBuildArgs() {
        val insecureChannel = mockk<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("localhost", 50051).usePlaintext().maxInboundMessageSize(DEFAULT_BUILD_ARGS.maxInboundMessageSize).build() } returns insecureChannel

        val builderSpy = spyk(GrpcChannelBuilder())
        every { builderSpy.testConnection(any(), any()) } just runs

        val grpcChannel = builderSpy.build()

        verifySequence {
            builderSpy.build(DEFAULT_BUILD_ARGS)
            builderSpy.testConnection(grpcChannel, false)
        }
    }

    @Test
    internal fun skipTestConnection() {
        val insecureChannel = mockk<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("localhost", 50051).usePlaintext().maxInboundMessageSize(7).build() } returns insecureChannel

        val builderSpy = spyk(GrpcChannelBuilder())

        val grpcChannel = builderSpy.build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 7))

        verifySequence {
            builderSpy.build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 7))
        }
        assertThat(grpcChannel.channel, equalTo(insecureChannel))
    }

    @Test
    internal fun passDebugFlagToTestConnection() {
        val insecureChannel = mockk<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("localhost", 50051).usePlaintext().maxInboundMessageSize(12).build() } returns insecureChannel

        val builderSpy = spyk(GrpcChannelBuilder())
        every { builderSpy.testConnection(any(), any()) } just runs

        val grpcChannel = builderSpy.build(GrpcBuildArgs(skipConnectionTest = false, debugConnectionTest = true, maxInboundMessageSize = 12))

        verifySequence {
            builderSpy.build(GrpcBuildArgs(skipConnectionTest=false, debugConnectionTest=true, maxInboundMessageSize = 12))
            builderSpy.testConnection(grpcChannel, true)
        }
    }

    @Test
    internal fun `testConnection returns on first successful call`() {
        val responses = mapOf(
            NetworkConsumerClient::class to StatusRuntimeException(Status.DATA_LOSS),
            CustomerConsumerClient::class to StatusRuntimeException(Status.INVALID_ARGUMENT),
            DiagramConsumerClient::class to null //null returns GrpcResult mockk with wasSuccessful = true
        )

        runTestConnection(responses)
    }

    @Test
    internal fun `testConnection rethrows grpc status UNAVAILABLE exceptions`() {
        val responses = mapOf(
            NetworkConsumerClient::class to StatusRuntimeException(Status.DATA_LOSS),
            CustomerConsumerClient::class to StatusRuntimeException(Status.INVALID_ARGUMENT),
            DiagramConsumerClient::class to StatusRuntimeException(Status.UNAVAILABLE),
        )

        expect {
            runTestConnection(responses)
        }.toThrow<StatusRuntimeException>().withStatusCode(Status.UNAVAILABLE)
    }

    @Test
    internal fun `testConnection rethrows grpc status UNAUTHENTICATED exceptions`() {
        val responses = mapOf(
            NetworkConsumerClient::class to StatusRuntimeException(Status.DATA_LOSS),
            CustomerConsumerClient::class to StatusRuntimeException(Status.UNAUTHENTICATED),
            DiagramConsumerClient::class to StatusRuntimeException(Status.UNAVAILABLE),
        )

        expect {
            runTestConnection(responses)
        }.toThrow<StatusRuntimeException>().withStatusCode(Status.UNAUTHENTICATED)
    }

    @Test
    internal fun `testConnection rethrows grpc status UNKNOWN exceptions`() {
        val responses = mapOf(
            NetworkConsumerClient::class to StatusRuntimeException(Status.DATA_LOSS),
            CustomerConsumerClient::class to StatusRuntimeException(Status.UNKNOWN),
            DiagramConsumerClient::class to StatusRuntimeException(Status.UNAVAILABLE),
        )

        expect {
            runTestConnection(responses)
        }.toThrow<StatusRuntimeException>().withStatusCode(Status.UNKNOWN)
    }

    @Test
    internal fun `testConnection rethrows unexpected exceptions`() {
        val responses = mapOf(
            NetworkConsumerClient::class to StatusRuntimeException(Status.DATA_LOSS),
            CustomerConsumerClient::class to Exception("Unexpected"),
            DiagramConsumerClient::class to StatusRuntimeException(Status.UNAVAILABLE),
        )

        expect {
            runTestConnection(responses)
        }.toThrow<Exception>().withMessage("Unexpected")
    }

    @Test
    internal fun `testConnection collects other exceptions for debug output`() {
        val responses = mapOf(
            NetworkConsumerClient::class to StatusRuntimeException(Status.DATA_LOSS.withDescription("Data loss message for testing")),
            CustomerConsumerClient::class to StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid argument message for testing")),
            DiagramConsumerClient::class to StatusRuntimeException(Status.UNIMPLEMENTED.withDescription("Method not found: zepben.protobuf.dc.DiagramConsumer/getMetadata")),
            )

        expect {
            runTestConnection(responses, debug = true)
        }.toThrow<GrpcConnectionException>().withMessage("Couldn't establish gRPC connection to any service on localhost:50051.\n" +
            "[DEBUG] com.zepben.evolve.streaming.get.NetworkConsumerClient: io.grpc.StatusRuntimeException: DATA_LOSS: Data loss message for testing\n" +
            "[DEBUG] com.zepben.evolve.streaming.get.CustomerConsumerClient: io.grpc.StatusRuntimeException: INVALID_ARGUMENT: Invalid argument message for testing\n" +
            "[DEBUG] com.zepben.evolve.streaming.get.DiagramConsumerClient: io.grpc.StatusRuntimeException: UNIMPLEMENTED: Method not found: zepben.protobuf.dc.DiagramConsumer/getMetadata")
    }

    @Test
    internal fun `testConnection throws GrpcConnectionException if no successful responses received`() {
        val responses = mapOf(
            NetworkConsumerClient::class to StatusRuntimeException(Status.DATA_LOSS.withDescription("Data loss message for testing")),
            CustomerConsumerClient::class to StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid argument message for testing")),
            DiagramConsumerClient::class to StatusRuntimeException(Status.UNIMPLEMENTED.withDescription("Method not found: zepben.protobuf.dc.DiagramConsumer/getMetadata")),
        )

        expect {
            runTestConnection(responses, debug = false)
        }.toThrow<GrpcConnectionException>().withMessage("Couldn't establish gRPC connection to any service on localhost:50051.")
    }

    @Test
    internal fun forAddress() {
        val insecureChannel = mockk<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234).usePlaintext().maxInboundMessageSize(any()).build() } returns insecureChannel

        val grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234)
            .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 28))
        assertThat(grpcChannel.channel, equalTo(insecureChannel))
    }

    @Test
    internal fun makeSecure() {
        val caFile = mockk<File>()
        val channelCredentials = mockk<ChannelCredentials>()
        val secureChannel = mockk<ManagedChannel>()

        mockkStatic(TlsChannelCredentials::class)
        every { TlsChannelCredentials.newBuilder().trustManager(caFile).build() } returns channelCredentials

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234, channelCredentials).maxInboundMessageSize(any()).build() } returns secureChannel

        val grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234).makeSecure(caFile).build(
            GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 34))
        assertThat(grpcChannel.channel, equalTo(secureChannel))
    }

    @Test
    internal fun makeSecureWithDefaultTrust() {
        val channelCredentials = mockk<ChannelCredentials>()
        val secureChannel = mockk<ManagedChannel>()

        mockkStatic(TlsChannelCredentials::class)
        every { TlsChannelCredentials.create() } returns channelCredentials

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234, channelCredentials).maxInboundMessageSize(any()).build() } returns secureChannel

        val grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234).makeSecure()
            .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 42))
        assertThat(grpcChannel.channel, equalTo(secureChannel))
    }

    @Test
    internal fun makeSecureWithClientAuthentication() {
        val caFile = mockk<File>()
        val pkFile = mockk<File>()
        val certChainFile = mockk<File>()
        val channelCredentials = mockk<ChannelCredentials>()
        val secureChannel = mockk<ManagedChannel>()

        mockkStatic(TlsChannelCredentials::class)
        every { TlsChannelCredentials.newBuilder().keyManager(certChainFile, pkFile).trustManager(caFile).build() } returns channelCredentials
        every { TlsChannelCredentials.newBuilder().keyManager(certChainFile, pkFile).build() } returns channelCredentials

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234, channelCredentials).maxInboundMessageSize(any()).build() } returns secureChannel

        assertThat(
            GrpcChannelBuilder().forAddress("hostname", 1234).makeSecure(caFile, certChainFile, pkFile)
                .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 53)).channel, equalTo(secureChannel)
        )
        assertThat(
            GrpcChannelBuilder().forAddress("hostname", 1234).makeSecure(certificateChain = certChainFile, privateKey = pkFile)
                .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 62)).channel, equalTo(secureChannel)
        )
    }

    @Test
    internal fun makeSecureWithFilenamesCoverage() {
        // There's no way of stubbing constructors themselves AFAIK, which would be useful in ensuring that the filename overload calls the original with
        // the File(...) of each filename.
        val secureGrpcChannel = mockk<GrpcChannel>()

        mockkConstructor(GrpcChannelBuilder::class)
        every {
            constructedWith<GrpcChannelBuilder>().makeSecure(null, "certChainFilename", "pkFilename")
                .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 1))
        } returns secureGrpcChannel

        assertThat(
            GrpcChannelBuilder().makeSecure(null, "certChainFilename", "pkFilename")
                .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 1)), equalTo(secureGrpcChannel)
        )
        assertThat(
            GrpcChannelBuilder().makeSecure(certificateChain = "certChainFilename", privateKey = "pkFilename")
                .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 1)), equalTo(secureGrpcChannel)
        )
    }

    @Test
    internal fun withTokenFetcher() {
        val authenticatedChannel = mockk<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234, any()).maxInboundMessageSize(any()).intercept(any<CallCredentialApplier>()).build() } returns authenticatedChannel

        val tokenFetcher = ZepbenTokenFetcher("audience", "domain", AuthMethod.AUTH0)
        val grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234).makeSecure().withTokenFetcher(tokenFetcher)
            .build(GrpcBuildArgs(skipConnectionTest = true, debugConnectionTest = false, maxInboundMessageSize = 1))
        assertThat(grpcChannel.channel, equalTo(authenticatedChannel))
    }

    private fun ExceptionMatcher<StatusRuntimeException>.withStatusCode(expected: Status): ExceptionMatcher<StatusRuntimeException> {
        val status = exception.status ?: throw ExpectExceptionError(expected.toString(), "")
        if (expected == status) return this
        throw ExpectExceptionError(expected.toString(), status.toString())
    }

    private fun runTestConnection(responses: Map<KClass<out CimConsumerClient<out BaseService, out BaseProtoToCim>>, Exception?>, debug: Boolean = false) {
        val builder = GrpcChannelBuilder()
        val channel = mockk<Channel>()
        val grpcChannel = mockk<GrpcChannel>()

        every { grpcChannel.channel } returns channel

        val networkGrpcResult = mockk<GrpcResult<ServiceInfo>>()
        mockkConstructor(NetworkConsumerClient::class)
        every { anyConstructed<NetworkConsumerClient>().getMetadata() } returns networkGrpcResult

        responses[NetworkConsumerClient::class]?.let {
            every { networkGrpcResult.thrown } returns it
            every { networkGrpcResult.wasSuccessful } returns false
        } ?: (every { networkGrpcResult.wasSuccessful } returns true)


        val customerGrpcResult = mockk<GrpcResult<ServiceInfo>>()
        mockkConstructor(CustomerConsumerClient::class)
        every { anyConstructed<CustomerConsumerClient>().getMetadata() } returns customerGrpcResult

        responses[CustomerConsumerClient::class]?.let {
            every { customerGrpcResult.thrown } returns it
            every { customerGrpcResult.wasSuccessful } returns false
        } ?: (every { customerGrpcResult.wasSuccessful } returns true)


        val diagramGrpcResult = mockk<GrpcResult<ServiceInfo>>()
        mockkConstructor(DiagramConsumerClient::class)
        every { anyConstructed<DiagramConsumerClient>().getMetadata() } returns diagramGrpcResult

        responses[DiagramConsumerClient::class]?.let {
            every { diagramGrpcResult.thrown } returns it
            every { diagramGrpcResult.wasSuccessful } returns false
        } ?: (every { diagramGrpcResult.wasSuccessful } returns true)

        builder.testConnection(grpcChannel, debug)
    }
}
