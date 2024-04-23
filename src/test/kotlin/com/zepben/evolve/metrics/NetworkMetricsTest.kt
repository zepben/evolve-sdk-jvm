package com.zepben.evolve.metrics

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.anEmptyMap
import org.junit.jupiter.api.Test

internal class NetworkMetricsTest {

    @Test
    internal fun defaultValue() {
        val metrics = NetworkMetrics()
        MatcherAssert.assertThat(metrics[TotalNetworkContainer], anEmptyMap())
    }

}
