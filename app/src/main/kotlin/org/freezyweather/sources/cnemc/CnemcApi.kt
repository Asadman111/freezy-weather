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

package org.freezyweather.sources.cnemc

import io.reactivex.rxjava3.core.Observable
import org.freezyweather.sources.cnemc.json.CnemcAllCityResult
import org.freezyweather.sources.cnemc.json.CnemcAqiPublishLiveInfoResult
import org.freezyweather.sources.cnemc.json.CnemcAqiPublishLiveResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * China National Environmental Monitoring Centre (CNEMC) API
 */
interface CnemcApi {
    /**
     * Get all city real-time AQI models - used to get cityCode mapping
     */
    @GET("CityData/GetAllCityRealTimeAQIModels")
    fun getAllCityRealTimeAQIModels(): Observable<List<CnemcAllCityResult>>

    /**
     * Get AQI data publish live by city name
     */
    @GET("CityData/GetAQIDataPublishLive")
    fun getAQIDataPublishLive(
        @Query("cityName") cityName: String,
    ): Observable<List<CnemcAqiPublishLiveResult>>

    /**
     * Get AQI data publish live info by city code
     */
    @GET("CityData/GetAQIDataPublishLiveInfo")
    fun getAQIDataPublishLiveInfo(
        @Query("cityCode") cityCode: String,
    ): Observable<CnemcAqiPublishLiveInfoResult>
}
