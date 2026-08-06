/*
 * This file is part of Freezy Weather.
 *
 * Freezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Freezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Freezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.freezyweather.domain.weather.index

import androidx.annotation.StringRes
import org.freezyweather.R

/**
 * Air quality index standard.
 * PLUME: Plume AQI 2023 standard
 * CHINA: China HJ 633-2012 standard
 */
enum class AirQualityStandard(
    val id: String,
    @StringRes val nameId: Int,
    @StringRes val descriptionId: Int,
    val aqiThresholds: List<Int>,
    val namesArrayId: Int,
    val descriptionsArrayId: Int,
    val harmlessExposuresArrayId: Int,
    val colorsArrayId: Int,
) {
    PLUME(
        "plume",
        R.string.air_quality_standard_plume,
        R.string.air_quality_standard_plume_description,
        listOf(0, 20, 50, 100, 150, 250),
        R.array.air_quality_levels,
        R.array.air_quality_level_descriptions,
        R.array.air_quality_level_harmless_exposures,
        R.array.air_quality_level_colors
    ),
    CHINA(
        "china",
        R.string.air_quality_standard_china,
        R.string.air_quality_standard_china_description,
        listOf(0, 50, 100, 150, 200, 300),
        R.array.china_air_quality_levels,
        R.array.china_air_quality_level_descriptions,
        R.array.china_air_quality_level_harmless_exposures,
        R.array.china_air_quality_level_colors
    ),
    ;

    companion object {
        fun getInstance(value: String): AirQualityStandard {
            return entries.firstOrNull { it.id == value } ?: PLUME
        }
    }
}
