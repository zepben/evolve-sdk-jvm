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
import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class GrpcChannelBuilderTest {

    @AfterEach
    internal fun teardownMockks() {
        unmockkAll()
    }

    @Test
    internal fun forAddress() {
        val insecureChannel = mock<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234).usePlaintext().build() } returns insecureChannel

        val grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234).build()
        assertThat(grpcChannel.channel, equalTo(insecureChannel))
    }

    @Test
    internal fun makeSecure() {
        val secureChannel = mock<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234, any()).build() } returns secureChannel

        val grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234).makeSecure().build()
        assertThat(grpcChannel.channel, equalTo(secureChannel))
    }

    @Test
    internal fun withTokenFetcher() {
        val authenticatedChannel = mock<ManagedChannel>()

        mockkStatic(NettyChannelBuilder::class)
        every { NettyChannelBuilder.forAddress("hostname", 1234, any()).intercept(any<CallCredentialApplier>()).build() } returns authenticatedChannel

        val tokenFetcher = ZepbenTokenFetcher("audience", "domain", AuthMethod.AUTH0)
        val grpcChannel = GrpcChannelBuilder().forAddress("hostname", 1234).makeSecure().withTokenFetcher(tokenFetcher).build()
        assertThat(grpcChannel.channel, equalTo(authenticatedChannel))
    }

}