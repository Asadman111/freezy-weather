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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import freezyweather.domain.location.model.Location
import freezyweather.domain.source.SourceFeature
import freezyweather.domain.weather.model.AirQuality
import freezyweather.domain.weather.wrappers.AirQualityWrapper
import freezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.freezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.freezyweather.sources.cnemc.json.CnemcAqiPublishLiveInfoResult
import org.freezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.freezyweather.unit.pollutant.PollutantConcentration.Companion.milligramsPerCubicMeter
import retrofit2.Retrofit
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Named

class CnemcService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : CnemcServiceStub(context) {

    private val mApi by lazy {
        client
            .baseUrl(CNEMC_BASE_URL)
            .build()
            .create(CnemcApi::class.java)
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        if (SourceFeature.AIR_QUALITY !in requestedFeatures) {
            return Observable.just(WeatherWrapper())
        }

        val failedFeatures = mutableMapOf<SourceFeature, Throwable>()

        // Get the city code from location parameters
        val cityCode = location.parameters
            .getOrElse(id) { null }
            ?.getOrElse("cityCode") { null }

        val aqiObservable: Observable<AirQuality> = if (!cityCode.isNullOrEmpty()) {
            // Direct lookup by city code
            mApi.getAQIDataPublishLiveInfo(cityCode)
                .map { result -> convertToAirQuality(result) }
                .onErrorResumeNext { error ->
                    failedFeatures[SourceFeature.AIR_QUALITY] = error
                    Observable.empty()
                }
        } else {
            // Need to find cityCode first - use city name from location
            val cityName = location.city
                ?: location.admin2
                ?: location.admin1
                ?: return Observable.just(
                    WeatherWrapper(
                        failedFeatures = failedFeatures.also {
                            it[SourceFeature.AIR_QUALITY] = InvalidOrIncompleteDataException()
                        }
                    )
                )

            // Try to find city by name, get its cityCode, then fetch AQI data
            mApi.getAQIDataPublishLive(URLEncoder.encode(cityName, "UTF-8"))
                .flatMap { results ->
                    if (results.isNotEmpty()) {
                        val sortedResults = results.sortedByDescending {
                            it.aqi?.toDoubleOrNull() ?: 0.0
                        }
                        val bestMatch = sortedResults.firstOrNull {
                            it.cityCode?.isNotEmpty() == true
                        }
                        if (bestMatch != null) {
                            mApi.getAQIDataPublishLiveInfo(bestMatch.cityCode!!)
                        } else {
                            // Fallback: try to find from all cities list
                            findAllCityCode(cityName)
                        }
                    } else {
                        findAllCityCode(cityName)
                    }
                }
                .map { result -> convertToAirQuality(result) }
                .onErrorResumeNext { error ->
                    failedFeatures[SourceFeature.AIR_QUALITY] = error
                    Observable.empty()
                }
        }

        return aqiObservable.map { airQuality ->
            WeatherWrapper(
                airQuality = AirQualityWrapper(current = airQuality),
                failedFeatures = failedFeatures
            )
        }
    }

    private fun findAllCityCode(
        cityName: String,
    ): Observable<CnemcAqiPublishLiveInfoResult> {
        return mApi.getAllCityRealTimeAQIModels()
            .flatMap { allCities ->
                val matchingCity = allCities.find {
                    it.cityName?.contains(cityName, ignoreCase = true) == true
                }
                if (matchingCity != null && matchingCity.cityCode != null) {
                    mApi.getAQIDataPublishLiveInfo(matchingCity.cityCode)
                } else {
                    Observable.error(InvalidOrIncompleteDataException())
                }
            }
    }

    private fun convertToAirQuality(
        result: org.freezyweather.sources.cnemc.json.CnemcAqiPublishLiveInfoResult,
    ): AirQuality {
        return AirQuality(
            pM25 = result.pm25?.toDoubleOrNull()?.microgramsPerCubicMeter,
            pM10 = result.pm10?.toDoubleOrNull()?.microgramsPerCubicMeter,
            sO2 = result.so2?.toDoubleOrNull()?.microgramsPerCubicMeter,
            nO2 = result.no2?.toDoubleOrNull()?.microgramsPerCubicMeter,
            o3 = result.o3?.toDoubleOrNull()?.microgramsPerCubicMeter,
            cO = result.co?.toDoubleOrNull()?.milligramsPerCubicMeter
        )
    }

    // Location parameters
    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean {
        if (coordinatesChanged) return true

        val currentCityCode = location.parameters
            .getOrElse(id) { null }
            ?.getOrElse("cityCode") { null }

        return currentCityCode.isNullOrEmpty()
    }

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        val cityName = location.city ?: location.admin2 ?: location.admin1
            ?: return Observable.error(InvalidOrIncompleteDataException())

        return mApi.getAQIDataPublishLive(URLEncoder.encode(cityName, "UTF-8"))
            .flatMap { results ->
                val sortedResults = results.sortedByDescending {
                    it.aqi?.toDoubleOrNull() ?: 0.0
                }
                val bestMatch = sortedResults.firstOrNull { it.cityCode?.isNotEmpty() == true }
                if (bestMatch != null) {
                    Observable.just(
                        mapOf(
                            "cityCode" to bestMatch.cityCode!!,
                            "cityName" to (bestMatch.positionName ?: cityName)
                        )
                    )
                } else {
                    // Fallback: try to find from all cities list
                    mApi.getAllCityRealTimeAQIModels()
                        .map { allCities ->
                            val matchingCity = allCities.find {
                                it.cityName?.contains(cityName, ignoreCase = true) == true
                            }
                            if (matchingCity != null && matchingCity.cityCode != null) {
                                mapOf(
                                    "cityCode" to matchingCity.cityCode,
                                    "cityName" to (matchingCity.cityName ?: cityName)
                                )
                            } else {
                                throw InvalidOrIncompleteDataException()
                            }
                        }
                }
            }
    }

    companion object {
        private const val CNEMC_BASE_URL = "https://air.cnemc.cn:18007/"
    }
}
