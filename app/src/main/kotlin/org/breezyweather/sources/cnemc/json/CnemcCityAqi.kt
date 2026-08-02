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

package org.breezyweather.sources.cnemc.json

import kotlinx.serialization.Serializable

/**
 * Item of https://air.cnemc.cn:18007/CityData/GetAllCityRealTimeAQIModels
 */
@Serializable
data class CnemcCityAqi(
    val TimePoint: String? = null,
    val AQI: String? = null,
    val AqiLevel: Int? = null,
    val PrimaryPollutant: String? = null,
    val Quality: String? = null,
    val Area: String? = null,
    val CityCode: Int? = null,
    val ProvinceId: Int? = null,
    val Latitude: String? = null,
    val Longitude: String? = null,
)

/**
 * Item of https://air.cnemc.cn:18007/CityData/GetAQIDataPublishLiveInfo?cityCode=xxxx
 * Concentrations are strings: PM2.5/PM10/SO2/NO2/O3 in µg/m³, CO in mg/m³
 */
@Serializable
data class CnemcCityAqiDetail(
    val TimePoint: String? = null,
    val AQI: String? = null,
    val AqiLevel: Int? = null,
    val PrimaryPollutant: String? = null,
    val Quality: String? = null,
    val Area: String? = null,
    val CityCode: Int? = null,
    val CO: String? = null,
    val NO2: String? = null,
    val O3: String? = null,
    val PM10: String? = null,
    val PM2_5: String? = null,
    val SO2: String? = null,
)
