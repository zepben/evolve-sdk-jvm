/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.streaming.grpc

import com.zepben.auth.client.ZepbenTokenFetcher
import com.zepben.evolve.streaming.get.CustomerConsumerClient
import com.zepben.evolve.streaming.get.DiagramConsumerClient
import com.zepben.evolve.streaming.get.NetworkConsumerClient
import com.zepben.protobuf.cc.CustomerConsumerGrpc
import com.zepben.protobuf.dc.DiagramConsumerGrpc
import com.zepben.protobuf.nc.NetworkConsumerGrpc
import io.grpc.*
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import java.io.File

private const val TWENTY_MEGABYTES = 1024 * 1024 * 20

data class GrpcBuildArgs(
    val skipConnectionTest: Boolean,
    val debugConnectionTest: Boolean,
    val maxInboundMessageSize: Int
)

val DEFAULT_BUILD_ARGS = GrpcBuildArgs(skipConnectionTest = false, debugConnectionTest = false, maxInboundMessageSize = TWENTY_MEGABYTES)


/**
 * Builder class for GrpcChannel. Allows easy specification of channel credentials via SSL/TLS
 * and call credentials via a ZepbenTokenFetcher.
 */
class GrpcChannelBuilder {

    private var _host: String = "localhost"
    private var _port: Int = 50051
    private var _channelCredentials: ChannelCredentials? = null
    private var _callCredentials: CallCredentials? = null

    fun build(buildArgs: GrpcBuildArgs = DEFAULT_BUILD_ARGS): GrpcChannel = GrpcChannel(
        _channelCredentials?.let { channelCreds ->
            val channelBuilder = NettyChannelBuilder.forAddress(_host, _port, channelCreds).maxInboundMessageSize(buildArgs.maxInboundMessageSize)
            _callCredentials?.let { callCreds ->
                channelBuilder.intercept(CallCredentialApplier(callCreds)).build()
            } ?: channelBuilder.build()
        } ?: NettyChannelBuilder.forAddress(_host, _port).usePlaintext().maxInboundMessageSize(buildArgs.maxInboundMessageSize).build()
    ).also {
        if (!buildArgs.skipConnectionTest)
            testConnection(it, buildArgs.debugConnectionTest)
    }

    internal fun testConnection(grpcChannel: GrpcChannel, debug: Boolean) {
        val clients = listOf(
            NetworkConsumerClient(NetworkConsumerGrpc.newStub(grpcChannel.channel)),
            CustomerConsumerClient(CustomerConsumerGrpc.newStub(grpcChannel.channel)),
            DiagramConsumerClient(DiagramConsumerGrpc.newStub(grpcChannel.channel)),
        )
        val debugErrors = mutableMapOf<String, StatusRuntimeException>()

        clients.forEach {
            val result = it.getMetadata()
            if (result.wasSuccessful)
                return
            val t = result.thrown
            if (t is StatusRuntimeException) {
                if (listOf(Status.Code.UNAUTHENTICATED, Status.Code.UNAVAILABLE, Status.Code.UNKNOWN).contains(t.status.code))
                    throw t
                else
                    if (debug)
                        debugErrors[it::class.java.name] = t
            } else
                throw t
        }
        var debugInfo = ""
        debugErrors.forEach{ debugInfo +=  "\n[DEBUG] ${it.key}: ${it.value}" }
        throw GrpcConnectionException("Couldn't establish gRPC connection to any service on $_host:$_port.${debugInfo}")
    }

    fun forAddress(
        host: String,
        port: Int
    ): GrpcChannelBuilder = apply {
        _host = host
        _port = port
    }

    fun makeSecure(
        rootCertificates: File
    ): GrpcChannelBuilder = apply {
        _channelCredentials = TlsChannelCredentials.newBuilder().trustManager(rootCertificates).build()
    }

    fun makeSecure(rootCertificates: String? = null): GrpcChannelBuilder = rootCertificates?.let {
        makeSecure(File(rootCertificates))
    } ?: apply {
        _channelCredentials = TlsChannelCredentials.create()
    }

    fun makeSecure(
        rootCertificates: File? = null,
        certificateChain: File,
        privateKey: File
    ): GrpcChannelBuilder = apply {
        var channelCredentialsBuilder = TlsChannelCredentials.newBuilder().keyManager(certificateChain, privateKey)
        if (rootCertificates != null) {
            channelCredentialsBuilder = channelCredentialsBuilder.trustManager(rootCertificates)
        }
        _channelCredentials = channelCredentialsBuilder.build()
    }

    fun makeSecure(
        rootCertificates: String? = null,
        certificateChain: String,
        privateKey: String
    ): GrpcChannelBuilder = makeSecure(rootCertificates?.let { File(it) }, File(certificateChain), File(privateKey))

    fun withTokenFetcher(tokenFetcher: ZepbenTokenFetcher): GrpcChannelBuilder = apply {
        _callCredentials = TokenCallCredentials(tokenFetcher::fetchToken)
    }

}
