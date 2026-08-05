package org.freezyweather.sources.veduris.json

import kotlinx.serialization.Serializable

@Serializable
data class VedurIsForecast(
    val featureCollection: VedurIsFeatureCollection?,
)
