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

package freezyweather.domain.weather.wrappers

import freezyweather.domain.weather.model.AirQuality
import freezyweather.domain.weather.model.Hourly
import freezyweather.domain.weather.model.Precipitation
import freezyweather.domain.weather.model.PrecipitationProbability
import freezyweather.domain.weather.model.UV
import freezyweather.domain.weather.model.Wind
import freezyweather.domain.weather.reference.WeatherCode
import org.freezyweather.unit.distance.Distance
import org.freezyweather.unit.pressure.Pressure
import org.freezyweather.unit.ratio.Ratio
import org.freezyweather.unit.temperature.Temperature
import java.util.Date
import kotlin.time.Duration

/**
 * Hourly wrapper that allows isDaylight to be null and completed later
 */
data class HourlyWrapper(
    val date: Date,
    val isDaylight: Boolean? = null,
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: TemperatureWrapper? = null,
    val precipitation: Precipitation? = null,
    val precipitationProbability: PrecipitationProbability? = null,
    val wind: Wind? = null,
    val uV: UV? = null,
    val relativeHumidity: Ratio? = null,
    val dewPoint: Temperature? = null,
    /**
     * Pressure at sea level
     * Use Kotlin extensions to initialize this value, like 1013.25.hectopascals
     */
    val pressure: Pressure? = null,
    val cloudCover: Ratio? = null,
    val visibility: Distance? = null,
    /**
     * Duration of sunshine, NOT duration of daylight
     */
    val sunshineDuration: Duration? = null,
) {
    fun toHourly(
        airQuality: AirQuality? = null,
        isDaylight: Boolean? = null,
        uV: UV? = null,
    ) = Hourly(
        date = this.date,
        isDaylight = isDaylight ?: this.isDaylight ?: true,
        weatherText = this.weatherText,
        weatherCode = this.weatherCode,
        temperature = this.temperature?.toTemperature(),
        precipitation = this.precipitation,
        precipitationProbability = this.precipitationProbability,
        wind = this.wind,
        airQuality = airQuality,
        uV = uV ?: this.uV,
        relativeHumidity = this.relativeHumidity,
        dewPoint = this.dewPoint,
        pressure = this.pressure,
        cloudCover = this.cloudCover,
        visibility = this.visibility
    )
}
