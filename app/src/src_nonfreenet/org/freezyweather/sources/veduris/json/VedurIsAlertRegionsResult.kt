package org.freezyweather.sources.veduris.json

import kotlinx.serialization.Serializable
import org.freezyweather.sources.veduris.serializers.VedurIsAnySerializer

@Serializable
data class VedurIsAlertRegionsResult(
    @Suppress("ktlint")
    val features: List<@Serializable(with = VedurIsAnySerializer::class) Any?> = listOf(),
)
