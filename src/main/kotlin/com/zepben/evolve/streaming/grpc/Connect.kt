/*
 * Copyright 2022 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.streaming.grpc

import com.zepben.auth.client.SSLContextUtils
import com.zepben.auth.client.ZepbenTokenFetcher
import com.zepben.auth.client.createTokenFetcher
import com.zepben.auth.client.createTokenFetcherManagedIdentity
import java.net.http.HttpClient

/**
 * A collection of functions that return a channel that connects to Evolve's gRPC service, given address and authentication details.
 */
object Connect {

    /**
     * Create a `GrpcChannel` that communicates with the gRPC service over plaintext.
     *
     * @param host The hostname where the gRPC service is hosted
     * @param rpcPort The port of the gRPC service
     * @return A plaintext connection to the gRPC service
     */
    @JvmStatic
    @JvmOverloads
    fun connectInsecure(
        host: String = "localhost",
        rpcPort: Int = 50051
    ): GrpcChannel =
        GrpcChannelBuilder().forAddress(host, rpcPort).build()

    /**
     * Create a `GrpcChannel` that communicates with the gRPC service using SSL/TLS transport security.
     *
     * @param host The hostname where the gRPC service is hosted
     * @param rpcPort The port of the gRPC service
     * @param caFilename The filename of a truststore containing additional trusted root certificates. This parameter is optional
     *                   and defaults to null, in which case only the system CAs are used to verify certificates.
     * @return An encrypted connection to the gRPC service
     */
    @JvmStatic
    @JvmOverloads
    fun connectTls(
        host: String = "localhost",
        rpcPort: Int = 50051,
        caFilename: String? = null
    ): GrpcChannel =
        GrpcChannelBuilder().forAddress(host, rpcPort).makeSecure(rootCertificates = caFilename).build()

    /**
     * Create a `GrpcChannel` that communicates with the gRPC service using SSL/TLS transport security and the OAuth client credentials flow.
     * The OAuth provider's domain and the "audience" parameter of the token request are fetched as JSON from a specified URL.
     *
     * @param clientId The client ID of the OAuth application to authenticate for
     * @param clientSecret The client secret of the OAuth application to authenticate for
     * @param confAddress The address of the authentication configuration
     * @param confCAFilename The filename of a truststore containing additional trusted root certificates for fetching the authentication configuration.
     *                       This parameter is optional and defaults to null, in which case only the system CAs are used to verify certificates.
     * @param authCAFilename The filename of a truststore containing additional trusted root certificates for fetching the OAuth tokens.
     *                       This parameter is optional and defaults to null, in which case only the system CAs are used to verify certificates.
     * @param host The hostname where the gRPC service is hosted
     * @param rpcPort The port of the gRPC service
     * @param caFilename The filename of a truststore containing additional trusted root certificates. This parameter is optional
     *                   and defaults to null, in which case only the system CAs are used to verify certificates.
     * @return An Auth0-authenticated, encrypted connection to the gRPC service. If the authentication configuration specifies that no authentication is
     *         required, a non-authenticated, encrypted connection is returned instead.
     */
    @JvmStatic
    @JvmOverloads
    fun connectWithSecret(
        clientId: String,
        clientSecret: String,
        host: String = "localhost",
        rpcPort: Int = 50051,
        confAddress: String? = null,
        confCAFilename: String? = null,
        authCAFilename: String? = null,
        caFilename: String? = null,
        verifyCertificates: Boolean = true
    ): GrpcChannel {
        val tokenFetcher = createTokenFetcher(
            confAddress ?: "https://$host/ewb/auth",
            confCAFilename = confCAFilename,
            authCAFilename = authCAFilename,
            verifyCertificates = verifyCertificates
        )

        return connectWithSecretUsingTokenFetcher(tokenFetcher, clientId, clientSecret, host, rpcPort, caFilename)
    }

    /**
     * Create a `GrpcChannel` that communicates with the gRPC service using SSL/TLS transport security and the OAuth client credentials flow.
     *
     * @param clientId The client ID of the OAuth application to authenticate for
     * @param clientSecret The client secret of the OAuth application to authenticate for
     * @param audience The audience parameter to be sent in token requests. This specifies the API to grant access for.
     * @param issuer The domain of the OAuth issuer.
     * @param authCAFilename The filename of a truststore containing additional trusted root certificates for fetching the OAuth tokens.
     *                       This parameter is optional and defaults to null, in which case only the system CAs are used to verify certificates.
     * @param host The hostname where the gRPC service is hosted
     * @param rpcPort The port of the gRPC service
     * @param caFilename The filename of a truststore containing additional trusted root certificates. This parameter is optional
     *                   and defaults to null, in which case only the system CAs are used to verify certificates.
     * @return An Auth0-authenticated, encrypted connection to the gRPC service
     */
    @JvmStatic
    @JvmOverloads
    fun connectWithSecret(
        clientId: String,
        clientSecret: String,
        audience: String,
        issuer: String,
        host: String = "localhost",
        rpcPort: Int = 50051,
        authCAFilename: String? = null,
        caFilename: String? = null
    ): GrpcChannel {

        val authClient = authCAFilename?.let {
            HttpClient.newBuilder().sslContext(SSLContextUtils.singleCACertSSLContext(it)).build()
        } ?: HttpClient.newBuilder().sslContext(SSLContextUtils.allTrustingSSLContext()).build()
        val tokenFetcher = createTokenFetcher(audience = audience, issuer = issuer, authClient = authClient, verifyCertificates = false)
        return connectWithSecretUsingTokenFetcher(tokenFetcher, clientId, clientSecret, host, rpcPort, caFilename)
    }

    /**
     * Create a `GrpcChannel` that communicates with the gRPC service using SSL/TLS transport security and the OAuth password grant flow.
     *
     * @param clientId The client ID of the OAuth application to authenticate for
     * @param username The username of the account registered with the OAuth application
     * @param password The password of the account registered with the OAuth application
     * @param confAddress The address of the authentication configuration
     * @param confCAFilename The filename of a truststore containing additional trusted root certificates for fetching the authentication configuration.
     *                       This parameter is optional and defaults to null, in which case only the system CAs are used to verify certificates.
     * @param authCAFilename The filename of a truststore containing additional trusted root certificates for fetching the OAuth tokens.
     *                       This parameter is optional and defaults to null, in which case only the system CAs are used to verify certificates.
     * @param host The hostname where the gRPC service is hosted
     * @param rpcPort The port of the gRPC service
     * @param caFilename The filename of a truststore containing additional trusted root certificates. This parameter is optional
     *                   and defaults to null, in which case only the system CAs are used to verify certificates.
     * @return An Auth0-authenticated, encrypted connection to the gRPC service. If the authentication configuration specifies that no authentication is
     *         required, a non-authenticated, encrypted connection is returned instead.
     */
    @JvmStatic
    @JvmOverloads
    fun connectWithPassword(
        clientId: String,
        username: String,
        password: String,
        host: String = "localhost",
        rpcPort: Int = 50051,
        confAddress: String? = null,
        confCAFilename: String? = null,
        authCAFilename: String? = null,
        caFilename: String? = null
    ): GrpcChannel {
        val tokenFetcher = createTokenFetcher(confAddress ?: "https://$host/ewb/auth", confCAFilename = confCAFilename, authCAFilename = authCAFilename)

        return connectWithPasswordUsingTokenFetcher(tokenFetcher, clientId, username, password, host, rpcPort, caFilename)
    }

    /**
     * Create a `GrpcChannel` that communicates with the gRPC service using SSL/TLS transport security and the OAuth client credentials flow.
     *
     * @param clientId The client ID of the OAuth application to authenticate for
     * @param username The username of the account registered with the OAuth application
     * @param password The password of the account registered with the OAuth application
     * @param audience The audience parameter to be sent in token requests. This specifies the API to grant access for.
     * @param issuer The domain of the OAuth issuer.
     * @param authCAFilename The filename of a truststore containing additional trusted root certificates for fetching the OAuth tokens.
     *                       This parameter is optional and defaults to null, in which case only the system CAs are used to verify certificates.
     * @param host The hostname where the gRPC service is hosted
     * @param rpcPort The port of the gRPC service
     * @param caFilename The filename of a truststore containing additional trusted root certificates. This parameter is optional
     *                   and defaults to null, in which case only the system CAs are used to verify certificates.
     * @return An Auth0-authenticated, encrypted connection to the gRPC service
     */
    @JvmStatic
    @JvmOverloads
    fun connectWithPassword(
        clientId: String,
        username: String,
        password: String,
        audience: String,
        issuer: String,
        host: String = "localhost",
        rpcPort: Int = 50051,
        authCAFilename: String? = null,
        caFilename: String? = null
    ): GrpcChannel {
        val authClient = authCAFilename?.let {
            HttpClient.newBuilder().sslContext(SSLContextUtils.singleCACertSSLContext(it)).build()
        } ?: HttpClient.newBuilder().sslContext(SSLContextUtils.allTrustingSSLContext()).build()
        val tokenFetcher = createTokenFetcher(audience = audience, issuer = issuer, authClient = authClient, verifyCertificates = false)

        return connectWithPasswordUsingTokenFetcher(tokenFetcher, clientId, username, password, host, rpcPort, caFilename)
    }

    /**
     * Create a `GrpcChannel` that communicates with the gRPC service using SSL/TLS transport security and the OAuth identity url
     *
     * @param identityUrl The identity URL for the VM this connection is attempted from
     * @param host The hostname where the gRPC service is hosted
     * @param rpcPort The port of the gRPC service
     * @param caFilename The filename of a truststore containing additional trusted root certificates. This parameter is optional
     *                   and defaults to null, in which case only the system CAs are used to verify certificates.
     * @return An Auth0-authenticated, encrypted connection to the gRPC service
     */
    @JvmStatic
    @JvmOverloads
    fun connectWithIdentity(
        identityUrl: String,
        host: String = "localhost",
        rpcPort: Int = 50051,
        caFilename: String? = null,
    ): GrpcChannel {
        val tokenFetcher = createTokenFetcherManagedIdentity(identityUrl)
        return GrpcChannelBuilder().forAddress(host, rpcPort).makeSecure(rootCertificates = caFilename).withTokenFetcher(tokenFetcher).build()
    }

    private fun connectWithSecretUsingTokenFetcher(
        tokenFetcher: ZepbenTokenFetcher,
        clientId: String,
        clientSecret: String,
        host: String,
        rpcPort: Int,
        caFilename: String?
    ): GrpcChannel {
        tokenFetcher.tokenRequestData.put("client_id", clientId)
        tokenFetcher.tokenRequestData.put("client_secret", clientSecret)
        tokenFetcher.tokenRequestData.put("grant_type", "client_credentials")

        return GrpcChannelBuilder().forAddress(host, rpcPort).makeSecure(rootCertificates = caFilename).withTokenFetcher(tokenFetcher).build()
    }

    private fun connectWithPasswordUsingTokenFetcher(
        tokenFetcher: ZepbenTokenFetcher,
        clientId: String,
        username: String,
        password: String,
        host: String,
        rpcPort: Int,
        caFilename: String?
    ): GrpcChannel {
        tokenFetcher.tokenRequestData.put("client_id", clientId)
        tokenFetcher.tokenRequestData.put("username", username)
        tokenFetcher.tokenRequestData.put("password", password)
        tokenFetcher.tokenRequestData.put("grant_type", "password")
        tokenFetcher.tokenRequestData.put("scope", "offline_access")

        return GrpcChannelBuilder().forAddress(host, rpcPort).makeSecure(rootCertificates = caFilename).withTokenFetcher(tokenFetcher).build()
    }

}
