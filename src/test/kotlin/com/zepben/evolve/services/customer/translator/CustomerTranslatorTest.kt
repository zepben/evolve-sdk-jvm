/*
 * Copyright 2021 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.services.customer.translator

import com.zepben.evolve.cim.iec61968.customers.Customer
import com.zepben.evolve.cim.iec61968.customers.CustomerAgreement
import com.zepben.evolve.cim.iec61968.customers.PricingStructure
import com.zepben.evolve.cim.iec61968.customers.Tariff
import com.zepben.evolve.cim.iec61970.base.core.IdentifiedObject
import com.zepben.evolve.cim.iec61970.base.core.NameType
import com.zepben.evolve.services.common.translator.toPb
import com.zepben.evolve.services.customer.CustomerService
import com.zepben.evolve.services.customer.CustomerServiceComparator
import com.zepben.evolve.services.customer.testdata.fillFields
import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.jupiter.api.fail

internal class CustomerTranslatorTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    private val comparator = CustomerServiceComparator()

    @Test
    internal fun convertsCorrectly() {
        val csToPb = CustomerCimToProto()

        /************ IEC61968 CUSTOMERS ************/
        validate({ Customer() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.addFromPb(csToPb.toPb(it)) })
        validate({ CustomerAgreement() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.addFromPb(csToPb.toPb(it)) })
        validate({ PricingStructure() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.addFromPb(csToPb.toPb(it)) })
        validate({ Tariff() }, { ns, it -> it.fillFields(ns) }, { ns, it -> ns.addFromPb(csToPb.toPb(it)) })
    }

    //
    // NOTE: NameType is not sent via any grpc messages at this stage, so test it separately
    //

    @Test
    internal fun createsNewNameType() {
        val pb = NameType("nt1 name").apply {
            description = "nt1 desc"
        }.toPb()

        val cim = CustomerService().addFromPb(pb)

        assertThat(cim.name, equalTo(pb.name))
        assertThat(cim.description, equalTo(pb.description))
    }

    @Test
    internal fun updatesExistingNameType() {
        val pb = NameType("nt1 name").apply {
            description = "nt1 desc"
        }.toPb()

        val nt = NameType("nt1 name")
        val cim = CustomerService().apply { addNameType(nt) }.addFromPb(pb)

        assertThat(cim, sameInstance(nt))
        assertThat(cim.description, equalTo(pb.description))
    }

    private inline fun <reified T : IdentifiedObject> validate(creator: () -> T, filler: (CustomerService, T) -> Unit, adder: (CustomerService, T) -> T?) {
        val cim = creator()
        val blankDifferences = comparator.compare(cim, adder(CustomerService(), cim)!!).differences
        assertThat("Failed to convert blank ${T::class.simpleName}:${blankDifferences}", blankDifferences, anEmptyMap())

        filler(CustomerService(), cim)

        val populatedDifferences = comparator.compare(cim, addWithUnresolvedReferences(cim, adder)).differences
        assertThat("Failed to convert populated ${T::class.simpleName}:${populatedDifferences}", populatedDifferences, anEmptyMap())
    }

    private inline fun <reified T : IdentifiedObject> addWithUnresolvedReferences(cim: T, adder: (CustomerService, T) -> T?): T {
        // We need to convert the populated item before we check the differences, so we can complete the unresolved references.
        val service = CustomerService()
        val convertedCim = adder(service, cim)!!
        service.unresolvedReferences().toList().forEach { (_, toMrid, resolver, _) ->
            try {
                // There are no abstract classes in the chain currently for the customer service. If they ever show up copy the code
                // form network to support them.
                resolver.toClass.getDeclaredConstructor(String::class.java).newInstance(toMrid).also { service.tryAdd(it) }
            } catch (e: Exception) {
                // If this fails you need to add a concrete type mapping to the abstractCreators map at the top of this class.
                fail("Failed to create unresolved reference for ${resolver.toClass}.", e)
            }
        }
        return convertedCim
    }

}
