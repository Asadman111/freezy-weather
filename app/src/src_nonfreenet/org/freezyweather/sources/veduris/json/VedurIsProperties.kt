package org.freezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsProperties(
    val station: VedurIsStation,
)
