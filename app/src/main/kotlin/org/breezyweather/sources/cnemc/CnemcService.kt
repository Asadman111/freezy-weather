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

package org.breezyweather.sources.cnemc

import android.content.Context
import breezyweather.domain.location.model.Location
import breezyweather.domain.source.SourceContinent
import breezyweather.domain.source.SourceFeature
import breezyweather.domain.weather.model.AirQuality
import breezyweather.domain.weather.wrappers.AirQualityWrapper
import breezyweather.domain.weather.wrappers.WeatherWrapper
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Observable
import org.breezyweather.common.source.HttpSource
import org.breezyweather.common.source.WeatherSource
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.breezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE
import org.breezyweather.sources.cnemc.json.CnemcCityAqi
import org.breezyweather.sources.cnemc.json.CnemcCityAqiDetail
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.microgramsPerCubicMeter
import org.breezyweather.unit.pollutant.PollutantConcentration.Companion.milligramsPerCubicMeter
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named

/**
 * China National Environmental Monitoring Centre (中国环境监测总站)
 *
 * Provides the official real-time China AQI (HJ 633-2012) for Chinese cities.
 * Data is published hourly.
 */
class CnemcService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : HttpSource(), WeatherSource {

    private val mMai: CnemcApi by lazy {
        client
            .baseUrl(CNEMC_BASE_URL)
            .build()
            .create(CnemcApi::class.java)
    }

    override val id = "cnemc"
    override val name = "CNEMC (中国环境监测总站)"
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = "https://www.cnemc.cn/"

    override val supportedFeatures = mapOf(
        SourceFeature.AIR_QUALITY to "中国环境监测总站 (CNEMC)"
    )
    override val attributionLinks = mapOf(
        "中国环境监测总站" to "https://www.cnemc.cn/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return feature == SourceFeature.AIR_QUALITY &&
            !location.countryCode.isNullOrEmpty() &&
            location.countryCode.equals("CN", ignoreCase = true)
    }

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            isFeatureSupportedForLocation(location, feature) -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        if (SourceFeature.AIR_QUALITY !in requestedFeatures) {
            return Observable.just(WeatherWrapper())
        }

        return mMai.getAllCityRealTimeAqiModels()
            .flatMap { cityList ->
                val nearestCity = getNearestCity(cityList, location)
                if (nearestCity == null || nearestCity.CityCode == null) {
                    Observable.just(WeatherWrapper())
                } else {
                    mMai.getAqiDataPublishLiveInfo(nearestCity.CityCode)
                        .map { detail ->
                            val airQuality = getAirQuality(detail)
                            if (airQuality == null) {
                                WeatherWrapper()
                            } else {
                                WeatherWrapper(
                                    airQuality = AirQualityWrapper(
                                        current = airQuality
                                    )
                                )
                            }
                        }
                }
            }
    }

    private fun getNearestCity(
        cityList: List<CnemcCityAqi>,
        location: Location,
    ): CnemcCityAqi? {
        val locationLatLng = LatLng(location.latitude, location.longitude)
        var nearestCity: CnemcCityAqi? = null
        var nearestDistance = Double.MAX_VALUE
        cityList.forEach { city ->
            val latitude = city.Latitude?.toDoubleOrNull() ?: return@forEach
            val longitude = city.Longitude?.toDoubleOrNull() ?: return@forEach
            val distance = SphericalUtil.computeDistanceBetween(
                locationLatLng,
                LatLng(latitude, longitude)
            )
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearestCity = city
            }
        }
        return nearestCity?.takeIf { nearestDistance <= MAX_CITY_DISTANCE_METERS }
    }

    private fun getAirQuality(detail: CnemcCityAqiDetail): AirQuality? {
        val aqi = detail.AQI?.toIntOrNull()
        val airQuality = AirQuality(
            pM25 = detail.PM2_5?.toDoubleOrNull()?.microgramsPerCubicMeter,
            pM10 = detail.PM10?.toDoubleOrNull()?.microgramsPerCubicMeter,
            sO2 = detail.SO2?.toDoubleOrNull()?.microgramsPerCubicMeter,
            nO2 = detail.NO2?.toDoubleOrNull()?.microgramsPerCubicMeter,
            o3 = detail.O3?.toDoubleOrNull()?.microgramsPerCubicMeter,
            cO = detail.CO?.toDoubleOrNull()?.milligramsPerCubicMeter,
            aqi = aqi
        )
        return airQuality.takeIf { it.isValid }
    }

    companion object {
        private const val CNEMC_BASE_URL = "https://air.cnemc.cn:18007/"
        private const val MAX_CITY_DISTANCE_METERS = 200_000.0 // 200 km
    }
}
