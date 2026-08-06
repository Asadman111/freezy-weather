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

package org.freezyweather.sources.cnemc.json

import kotlinx.serialization.Serializable

@Serializable
data class CnemcAqiPublishLiveResult(
    val stationCode: String? = null,
    val cityCode: String? = null,
    val area: String? = null,
    val positionName: String? = null,
    val primaryPollutant: String? = null,
    val aqi: String? = null,
    val quality: String? = null,
    val pm25: String? = null,
    val pm10: String? = null,
    val so2: String? = null,
    val no2: String? = null,
    val o3: String? = null,
    val co: String? = null,
    val timePoint: String? = null,
    val longitude: String? = null,
    val latitude: String? = null,
)
