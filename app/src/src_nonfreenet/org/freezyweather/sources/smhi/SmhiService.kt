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

package org.freezyweather.sources.smhi

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import freezyweather.domain.location.model.Location
import freezyweather.domain.source.SourceFeature
import freezyweather.domain.weather.model.Precipitation
import freezyweather.domain.weather.model.PrecipitationProbability
import freezyweather.domain.weather.model.Wind
import freezyweather.domain.weather.reference.WeatherCode
import freezyweather.domain.weather.wrappers.DailyWrapper
import freezyweather.domain.weather.wrappers.HourlyWrapper
import freezyweather.domain.weather.wrappers.TemperatureWrapper
import freezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.freezyweather.common.exceptions.InvalidOrIncompleteDataException
import org.freezyweather.common.extensions.getIsoFormattedDate
import org.freezyweather.common.extensions.toDateNoHour
import org.freezyweather.sources.smhi.json.SmhiTimeSeries
import org.freezyweather.unit.distance.Distance.Companion.kilometers
import org.freezyweather.unit.precipitation.Precipitation.Companion.millimeters
import org.freezyweather.unit.pressure.Pressure.Companion.hectopascals
import org.freezyweather.unit.ratio.Ratio.Companion.percent
import org.freezyweather.unit.speed.Speed.Companion.metersPerSecond
import org.freezyweather.unit.temperature.Temperature.Companion.celsius
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.roundToInt

class SmhiService @Inject constructor(
    @ApplicationContext context: Context,
    @Named("JsonClient") client: Retrofit.Builder,
) : SmhiServiceStub(context) {

    override val privacyPolicyUrl =
        "https://www.smhi.se/om-smhi/smhis-hantering-av-personuppgifter-och-integritetspolicy"

    private val mApi by lazy {
        client
            .baseUrl(SMHI_BASE_URL)
            .build()
            .create(SmhiApi::class.java)
    }

    override val attributionLinks = mapOf(
        "SMHI" to "https://www.smhi.se/"
    )

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        return mApi.getForecast(
            location.longitude,
            location.latitude
        ).map {
            // If the API doesn’t return data, consider data as garbage and keep cached data
            if (it.timeSeries.isNullOrEmpty()) {
                throw InvalidOrIncompleteDataException()
            }

            WeatherWrapper(
                dailyForecast = getDailyForecast(location, it.timeSeries),
                hourlyForecast = getHourlyForecast(it.timeSeries)
            )
        }
    }

    private fun getDailyForecast(
        location: Location,
        forecastResult: List<SmhiTimeSeries>,
    ): List<DailyWrapper> {
        val dailyList = mutableListOf<DailyWrapper>()
        val hourlyListByDay = forecastResult.groupBy {
            it.time.getIsoFormattedDate(location)
        }
        for (i in 0 until hourlyListByDay.entries.size - 1) {
            val dayDate = hourlyListByDay.keys.toTypedArray()[i].toDateNoHour(location.timeZone)
            if (dayDate != null) {
                dailyList.add(DailyWrapper(date = dayDate))
            }
        }
        return dailyList
    }

    /**
     * Returns hourly forecast
     */
    private fun getHourlyForecast(
        forecastResult: List<SmhiTimeSeries>,
    ): List<HourlyWrapper> {
        return forecastResult.map { result ->
            HourlyWrapper(
                date = result.time,
                weatherCode = getWeatherCode(result.data.symbolCode),
                temperature = TemperatureWrapper(
                    temperature = result.data.airTemperature?.celsius
                ),
                precipitation = Precipitation(
                    total = result.data.precipitationAmountMean?.millimeters
                ),
                precipitationProbability = PrecipitationProbability(
                    thunderstorm = result.data.thunderstormProbability?.percent
                ),
                wind = Wind(
                    degree = result.data.windFromDirection,
                    speed = result.data.windSpeed?.metersPerSecond,
                    gusts = result.data.windSpeedOfGust?.metersPerSecond
                ),
                relativeHumidity = result.data.relativeHumidity?.percent,
                pressure = result.data.airPressureAtMeanSeaLevel?.hectopascals,
                visibility = result.data.visibilityInAir?.kilometers
            )
        }
    }

    private fun getWeatherCode(icon: Double?): WeatherCode? {
        if (icon == null) return null
        return when (icon.roundToInt()) {
            1, 2 -> WeatherCode.CLEAR
            3, 4 -> WeatherCode.PARTLY_CLOUDY
            5, 6 -> WeatherCode.CLOUDY
            7 -> WeatherCode.FOG
            8, 9, 10, 18, 19, 20 -> WeatherCode.RAIN
            12, 13, 14, 22, 23, 24 -> WeatherCode.SLEET
            15, 16, 17, 25, 26, 27 -> WeatherCode.SNOW
            11 -> WeatherCode.THUNDERSTORM
            21 -> WeatherCode.THUNDER
            else -> null
        }
    }

    companion object {
        private const val SMHI_BASE_URL = "https://opendata-download-metfcst.smhi.se/api/"
    }
}
