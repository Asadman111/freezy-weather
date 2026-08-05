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

import freezyweather.domain.weather.model.Current
import freezyweather.domain.weather.model.UV
import freezyweather.domain.weather.model.Wind
import freezyweather.domain.weather.reference.WeatherCode
import org.freezyweather.unit.distance.Distance
import org.freezyweather.unit.pressure.Pressure
import org.freezyweather.unit.ratio.Ratio
import org.freezyweather.unit.temperature.Temperature

/**
 * Current wrapper
 */
data class CurrentWrapper(
    val weatherText: String? = null,
    val weatherCode: WeatherCode? = null,
    val temperature: TemperatureWrapper? = null,
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
    val ceiling: Distance? = null,
    val dailyForecast: String? = null,
    // Is actually a description of the nowcast
    val hourlyForecast: String? = null,
) {
    fun toCurrent(
        uV: UV? = null,
    ) = Current(
        weatherText = this.weatherText,
        weatherCode = this.weatherCode,
        temperature = this.temperature?.toTemperature(),
        wind = this.wind,
        uV = uV ?: this.uV,
        relativeHumidity = this.relativeHumidity,
        dewPoint = this.dewPoint,
        pressure = this.pressure,
        cloudCover = this.cloudCover,
        visibility = this.visibility,
        ceiling = this.ceiling,
        dailyForecast = this.dailyForecast,
        hourlyForecast = this.hourlyForecast
    )
}
