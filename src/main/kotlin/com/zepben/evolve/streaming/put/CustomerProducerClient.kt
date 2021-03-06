/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.evolve.streaming.put

import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.services.common.translator.toPb
import com.zepben.evolve.services.customer.CustomerService
import com.zepben.evolve.services.customer.translator.toPb
import com.zepben.evolve.services.customer.whenCustomerServiceObject
import com.zepben.evolve.streaming.grpc.GrpcChannel
import com.zepben.protobuf.cp.*
import io.grpc.CallCredentials
import io.grpc.ManagedChannel

/**
 * Producer client for a [CustomerService].
 *
 * @property stub The gRPC stub to be used to communicate with the server
 */
class CustomerProducerClient(
    private val stub: CustomerProducerGrpc.CustomerProducerBlockingStub,
) : CimProducerClient<CustomerService>() {

    /**
     * Create a [CustomerProducerClient]
     *
     * @param channel [ManagedChannel] to build a blocking stub from.
     * @param callCredentials [CallCredentials] to be attached to the stub.
     */
    @JvmOverloads
    constructor(channel: ManagedChannel, callCredentials: CallCredentials? = null) :
        this(callCredentials?.let { CustomerProducerGrpc.newBlockingStub(channel).withCallCredentials(callCredentials) }
            ?: CustomerProducerGrpc.newBlockingStub(channel))

    /**
     * Create a [CustomerProducerClient]
     *
     * @param channel [GrpcChannel] to build a blocking stub from.
     * @param callCredentials [CallCredentials] to be attached to the stub.
     */
    @JvmOverloads
    constructor(channel: GrpcChannel, callCredentials: CallCredentials? = null) :
        this(callCredentials?.let { CustomerProducerGrpc.newBlockingStub(channel.channel).withCallCredentials(callCredentials) }
            ?: CustomerProducerGrpc.newBlockingStub(channel.channel))


    override fun send(service: CustomerService) {
        tryRpc { stub.createCustomerService(CreateCustomerServiceRequest.newBuilder().build()) }
            .throwOnUnhandledError()

        service.sequenceOf<IdentifiedObject>().forEach { sendToServer(it) }

        tryRpc { stub.completeCustomerService(CompleteCustomerServiceRequest.newBuilder().build()) }
            .throwOnUnhandledError()
    }

    private fun sendToServer(identifiedObject: IdentifiedObject) = tryRpc {
        whenCustomerServiceObject(
            identifiedObject,
            isCustomer = {
                val builder = CreateCustomerRequest.newBuilder().apply { customer = it.toPb() }.build()
                stub.createCustomer(builder)
            },
            isCustomerAgreement = {
                val builder = CreateCustomerAgreementRequest.newBuilder().apply { customerAgreement = it.toPb() }.build()
                stub.createCustomerAgreement(builder)
            },
            isOrganisation = {
                val builder = CreateOrganisationRequest.newBuilder().setOrganisation(it.toPb()).build()
                stub.createOrganisation(builder)
            },
            isPricingStructure = {
                val builder = CreatePricingStructureRequest.newBuilder().apply { pricingStructure = it.toPb() }.build()
                stub.createPricingStructure(builder)
            },
            isTariff = {
                val builder = CreateTariffRequest.newBuilder().apply { tariff = it.toPb() }.build()
                stub.createTariff(builder)
            }
        )
    }
        .throwOnUnhandledError()

}
