/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.domain.weather.model

import android.content.Context
import androidx.annotation.ColorInt
import breezyweather.domain.weather.model.AirQuality
import org.breezyweather.common.options.AirQualityIndexType
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.domain.weather.index.PollutantIndex

val AirQuality.validPollutants: List<PollutantIndex>
    get() {
        return listOf(
            PollutantIndex.NO2,
            PollutantIndex.O3,
            PollutantIndex.PM10,
            PollutantIndex.PM25,
            PollutantIndex.SO2,
            PollutantIndex.CO
        ).filter { getConcentration(it) != null }
    }

fun AirQuality.getIndex(pollutant: PollutantIndex? = null): Int? =
    getIndex(pollutant, AirQualityIndexType.INTERNATIONAL)

fun AirQuality.getIndex(type: AirQualityIndexType): Int? =
    getIndex(null, type)

fun AirQuality.getIndex(context: Context): Int? =
    getIndex(null, SettingsManager.getInstance(context).airQualityIndexType)

fun AirQuality.getIndex(
    pollutant: PollutantIndex? = null,
    type: AirQualityIndexType,
): Int? {
    return if (pollutant == null) { // Air Quality
        // Use the source-authoritative AQI (e.g. the official CNEMC China AQI) when it is provided
        // and the China standard is selected, otherwise compute it from concentrations
        if (type == AirQualityIndexType.CHINA && aqi != null) {
            aqi
        } else {
            val pollutantsAqi: List<Int> = listOfNotNull(
                getIndex(PollutantIndex.O3, type),
                getIndex(PollutantIndex.NO2, type),
                getIndex(PollutantIndex.PM10, type),
                getIndex(PollutantIndex.PM25, type)
            )
            if (pollutantsAqi.isNotEmpty()) pollutantsAqi.max() else null
        }
    } else { // Specific pollutant
        pollutant.getIndex(getConcentration(pollutant), type)
    }
}

fun AirQuality.getConcentration(pollutant: PollutantIndex) = when (pollutant) {
    PollutantIndex.PM25 -> pM25?.inMicrogramsPerCubicMeter
    PollutantIndex.PM10 -> pM10?.inMicrogramsPerCubicMeter
    PollutantIndex.O3 -> o3?.inMicrogramsPerCubicMeter
    PollutantIndex.NO2 -> nO2?.inMicrogramsPerCubicMeter
    PollutantIndex.SO2 -> sO2?.inMicrogramsPerCubicMeter
    PollutantIndex.CO -> cO?.inMilligramsPerCubicMeter
}

fun AirQuality.getName(context: Context, pollutant: PollutantIndex? = null): String? {
    val type = SettingsManager.getInstance(context).airQualityIndexType
    return if (pollutant == null) { // Air Quality
        PollutantIndex.getAqiToName(context, getIndex(null, type), type)
    } else { // Specific pollutant
        pollutant.getName(context, getConcentration(pollutant), type)
    }
}

fun AirQuality.getDescription(context: Context, pollutant: PollutantIndex? = null): String? {
    val type = SettingsManager.getInstance(context).airQualityIndexType
    return if (pollutant == null) { // Air Quality
        PollutantIndex.getAqiToDescription(context, getIndex(null, type), type)
    } else { // Specific pollutant
        pollutant.getDescription(context, getConcentration(pollutant), type)
    }
}

@ColorInt
fun AirQuality.getColor(context: Context, pollutant: PollutantIndex? = null): Int {
    val type = SettingsManager.getInstance(context).airQualityIndexType
    return if (pollutant == null) {
        PollutantIndex.getAqiToColor(context, getIndex(null, type), type)
    } else { // Specific pollutant
        pollutant.getColor(context, getConcentration(pollutant), type)
    }
}
