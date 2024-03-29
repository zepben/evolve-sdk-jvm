/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.evolve.cim.iec61968.common

import com.zepben.testutils.junit.SystemLogExtension
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Instant

internal class DocumentTest {

    @JvmField
    @RegisterExtension
    var systemErr: SystemLogExtension = SystemLogExtension.SYSTEM_ERR.captureLog().muteOnSuccess()

    @Test
    internal fun constructorCoverage() {
        assertThat(object : Document() {}.mRID, not(equalTo("")))
        assertThat(object : Document("id") {}.mRID, equalTo("id"))
    }

    @Test
    internal fun accessorCoverage() {
        val document = object : Document() {}

        assertThat(document.title, equalTo(""))
        assertThat(document.createdDateTime, nullValue())
        assertThat(document.authorName, equalTo(""))
        assertThat(document.type, equalTo(""))
        assertThat(document.status, equalTo(""))
        assertThat(document.comment, equalTo(""))

        document.apply {
            title = "title"
            createdDateTime = Instant.ofEpochMilli(1234)
            authorName = "authorName"
            type = "type"
            status = "status"
            comment = "comment"
        }

        assertThat(document.title, equalTo("title"))
        assertThat(document.createdDateTime, equalTo(Instant.ofEpochMilli(1234)))
        assertThat(document.authorName, equalTo("authorName"))
        assertThat(document.type, equalTo("type"))
        assertThat(document.status, equalTo("status"))
        assertThat(document.comment, equalTo("comment"))
    }
}
