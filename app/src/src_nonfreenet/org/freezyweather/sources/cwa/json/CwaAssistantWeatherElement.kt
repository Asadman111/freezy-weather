package org.freezyweather.sources.cwa.json

import kotlinx.serialization.Serializable

@Serializable
data class CwaAssistantWeatherElement(
    val ElementValue: CwaAssistantElementValue? = null,
)
