/*
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.freezyweather.background.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import freezyweather.data.location.LocationRepository
import freezyweather.data.weather.WeatherRepository
import freezyweather.domain.location.model.Location
import freezyweather.domain.source.SourceFeature
import freezyweather.domain.weather.model.AirQuality
import freezyweather.domain.weather.model.Alert
import freezyweather.domain.weather.model.Current
import freezyweather.domain.weather.model.Daily
import freezyweather.domain.weather.model.HalfDay
import freezyweather.domain.weather.model.Hourly
import freezyweather.domain.weather.model.Minutely
import freezyweather.domain.weather.model.Normals
import freezyweather.domain.weather.model.Pollen
import freezyweather.domain.weather.model.PrecipitationDuration
import freezyweather.domain.weather.model.PrecipitationProbability
import freezyweather.domain.weather.model.UV
import freezyweather.domain.weather.model.Wind
import freezyweather.domain.weather.reference.Month
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.freezyweather.BuildConfig
import org.freezyweather.common.extensions.getBeaufortScaleColor
import org.freezyweather.common.extensions.getBeaufortScaleStrength
import org.freezyweather.common.extensions.getCloudCoverDescription
import org.freezyweather.common.extensions.getVisibilityDescription
import org.freezyweather.common.extensions.gzipCompress
import org.freezyweather.common.extensions.roundDecimals
import org.freezyweather.common.source.HttpSource
import org.freezyweather.common.source.PollenIndexSource
import org.freezyweather.datasharing.json.FreezyAirQuality
import org.freezyweather.datasharing.json.FreezyAlert
import org.freezyweather.datasharing.json.FreezyBulletin
import org.freezyweather.datasharing.json.FreezyCurrent
import org.freezyweather.datasharing.json.FreezyDaily
import org.freezyweather.datasharing.json.FreezyDailyUnit
import org.freezyweather.datasharing.json.FreezyDegreeDay
import org.freezyweather.datasharing.json.FreezyHalfDay
import org.freezyweather.datasharing.json.FreezyHourly
import org.freezyweather.datasharing.json.FreezyMinutely
import org.freezyweather.datasharing.json.FreezyNormals
import org.freezyweather.datasharing.json.FreezyPollen
import org.freezyweather.datasharing.json.FreezyPollutant
import org.freezyweather.datasharing.json.FreezyPrecipitation
import org.freezyweather.datasharing.json.FreezyPrecipitationDuration
import org.freezyweather.datasharing.json.FreezyPrecipitationProbability
import org.freezyweather.datasharing.json.FreezySource
import org.freezyweather.datasharing.json.FreezyTemperature
import org.freezyweather.datasharing.json.FreezyUnit
import org.freezyweather.datasharing.json.FreezyWeather
import org.freezyweather.datasharing.json.FreezyWind
import org.freezyweather.datasharing.provider.ProviderLocation
import org.freezyweather.datasharing.provider.ProviderUri
import org.freezyweather.datasharing.provider.ProviderVersion
import org.freezyweather.domain.settings.SettingsManager
import org.freezyweather.domain.source.resourceName
import org.freezyweather.domain.weather.index.PollutantIndex
import org.freezyweather.domain.weather.model.getColor
import org.freezyweather.domain.weather.model.getConcentration
import org.freezyweather.domain.weather.model.getIndex
import org.freezyweather.domain.weather.model.getIndexName
import org.freezyweather.domain.weather.model.getLevel
import org.freezyweather.domain.weather.model.getMinutelyDescription
import org.freezyweather.domain.weather.model.getMinutelyTitle
import org.freezyweather.domain.weather.model.getName
import org.freezyweather.domain.weather.model.getUVColor
import org.freezyweather.domain.weather.model.validPollens
import org.freezyweather.domain.weather.model.validPollutants
import org.freezyweather.sources.SourceManager
import org.freezyweather.unit.distance.Distance
import org.freezyweather.unit.distance.DistanceUnit
import org.freezyweather.unit.pollen.PollenConcentrationUnit
import org.freezyweather.unit.precipitation.Precipitation
import org.freezyweather.unit.precipitation.PrecipitationUnit
import org.freezyweather.unit.pressure.Pressure
import org.freezyweather.unit.pressure.PressureUnit
import org.freezyweather.unit.ratio.Ratio
import org.freezyweather.unit.speed.Speed
import org.freezyweather.unit.speed.SpeedUnit
import org.freezyweather.unit.temperature.Temperature
import org.freezyweather.unit.temperature.TemperatureUnit
import kotlin.time.Duration

class WeatherContentProvider : ContentProvider() {

    private val hexFormat: HexFormat = HexFormat {
        upperCase = false
        number {
            prefix = "#"
            minLength = 6
            removeLeadingZeros = true
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface LocationRepositoryContentProviderEntryPoint {
        fun locationRepository(): LocationRepository
    }

    private fun getLocationRepository(appContext: Context): LocationRepository {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            LocationRepositoryContentProviderEntryPoint::class.java
        )
        return hiltEntryPoint.locationRepository()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface WeatherRepositoryContentProviderEntryPoint {
        fun weatherRepository(): WeatherRepository
    }

    private fun getWeatherRepository(appContext: Context): WeatherRepository {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            WeatherRepositoryContentProviderEntryPoint::class.java
        )
        return hiltEntryPoint.weatherRepository()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface SourceManagerContentProviderEntryPoint {
        fun sourceManager(): SourceManager
    }

    private fun getSourceManager(appContext: Context): SourceManager {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            SourceManagerContentProviderEntryPoint::class.java
        )
        return hiltEntryPoint.sourceManager()
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? {
        if (!isAllowedPackage()) {
            Log.w(TAG, "Content provider is disabled for this app")
            return null
        }

        return when (uriMatcher.match(uri)) {
            ProviderUri.VERSION_CODE -> queryVersion()
            ProviderUri.LOCATIONS_CODE -> queryLocations(
                limit = uri.getQueryParameter("limit")?.toIntOrNull()
            )
            ProviderUri.WEATHER_CODE -> queryWeather(
                selection = selection,
                withDailyQuery = uri.getQueryParameter("withDaily"),
                withHourlyQuery = uri.getQueryParameter("withHourly"),
                withMinutelyQuery = uri.getQueryParameter("withMinutely"),
                withAlertsQuery = uri.getQueryParameter("withAlerts"),
                withNormalsQuery = uri.getQueryParameter("withNormals"),
                temperatureUnitQuery = uri.getQueryParameter("temperatureUnit"),
                precipitationUnitQuery = uri.getQueryParameter("precipitationUnit"),
                speedUnitQuery = uri.getQueryParameter("speedUnit"),
                distanceUnitQuery = uri.getQueryParameter("distanceUnit"),
                pressureUnitQuery = uri.getQueryParameter("pressureUnit")
            )
            else -> {
                Log.w(TAG, "Unrecognized URI $uri")
                null
            }
        }
    }

    private fun isAllowedPackage(): Boolean {
        // Can check callingPackage here
        return true
    }

    private fun queryVersion(): Cursor {
        val columns = arrayOf(ProviderVersion.COLUMN_MAJOR, ProviderVersion.COLUMN_MINOR)
        val matrixCursor = MatrixCursor(columns).apply {
            addRow(arrayOf(ProviderVersion.MAJOR, ProviderVersion.MINOR))
        }
        return matrixCursor
    }

    private fun queryLocations(
        limit: Int?,
    ): Cursor {
        val columns = arrayOf(
            ProviderLocation.COLUMN_ID,
            ProviderLocation.COLUMN_LATITUDE,
            ProviderLocation.COLUMN_LONGITUDE,
            ProviderLocation.COLUMN_IS_CURRENT_POSITION,
            ProviderLocation.COLUMN_TIMEZONE,
            ProviderLocation.COLUMN_CUSTOM_NAME,
            ProviderLocation.COLUMN_COUNTRY,
            ProviderLocation.COLUMN_COUNTRY_CODE,
            ProviderLocation.COLUMN_ADMIN1,
            ProviderLocation.COLUMN_ADMIN1_CODE,
            ProviderLocation.COLUMN_ADMIN2,
            ProviderLocation.COLUMN_ADMIN2_CODE,
            ProviderLocation.COLUMN_ADMIN3,
            ProviderLocation.COLUMN_ADMIN3_CODE,
            ProviderLocation.COLUMN_ADMIN4,
            ProviderLocation.COLUMN_ADMIN4_CODE,
            ProviderLocation.COLUMN_CITY,
            ProviderLocation.COLUMN_DISTRICT,
            ProviderLocation.COLUMN_WEATHER
        )
        val locations = runBlocking {
            if (limit != null && limit > 0) {
                getLocationRepository(context!!).getXLocations(limit, withParameters = false)
            } else {
                getLocationRepository(context!!).getAllLocations(withParameters = false)
            }
        }
        val matrixCursor = MatrixCursor(columns).apply {
            locations.forEach { location ->
                addRow(
                    arrayOf<Any?>(
                        location.formattedId,
                        location.latitude,
                        location.longitude,
                        if (location.isCurrentPosition) 1 else 0,
                        location.timeZone.id,
                        location.customName,
                        location.country,
                        location.countryCode,
                        location.admin1,
                        location.admin1Code,
                        location.admin2,
                        location.admin2Code,
                        location.admin3,
                        location.admin3Code,
                        location.admin4,
                        location.admin4Code,
                        location.city,
                        location.district,
                        null
                    )
                )
            }
        }
        return matrixCursor
    }

    private fun queryWeather(
        selection: String?,
        withDailyQuery: String?,
        withHourlyQuery: String?,
        withMinutelyQuery: String?,
        withAlertsQuery: String?,
        withNormalsQuery: String?,
        temperatureUnitQuery: String?,
        precipitationUnitQuery: String?,
        speedUnitQuery: String?,
        distanceUnitQuery: String?,
        pressureUnitQuery: String?,
    ): Cursor {
        val columns = arrayOf(
            ProviderLocation.COLUMN_ID,
            ProviderLocation.COLUMN_LATITUDE,
            ProviderLocation.COLUMN_LONGITUDE,
            ProviderLocation.COLUMN_IS_CURRENT_POSITION,
            ProviderLocation.COLUMN_TIMEZONE,
            ProviderLocation.COLUMN_CUSTOM_NAME,
            ProviderLocation.COLUMN_COUNTRY,
            ProviderLocation.COLUMN_COUNTRY_CODE,
            ProviderLocation.COLUMN_ADMIN1,
            ProviderLocation.COLUMN_ADMIN1_CODE,
            ProviderLocation.COLUMN_ADMIN2,
            ProviderLocation.COLUMN_ADMIN2_CODE,
            ProviderLocation.COLUMN_ADMIN3,
            ProviderLocation.COLUMN_ADMIN3_CODE,
            ProviderLocation.COLUMN_ADMIN4,
            ProviderLocation.COLUMN_ADMIN4_CODE,
            ProviderLocation.COLUMN_CITY,
            ProviderLocation.COLUMN_DISTRICT,
            ProviderLocation.COLUMN_WEATHER
        )
        val matrixCursor = MatrixCursor(columns)
        if (selection == null) return matrixCursor

        val regex = Regex("id *= *(-?[0-9.]+)&(-?[0-9.]+)&([a-z]+)")
        val matching = regex.find(selection)
        val locationId = if (matching == null) {
            val regex2 = Regex("id *= *${Location.CURRENT_POSITION_ID}")
            val matching2 = regex2.find(selection)
            if (matching2 == null) {
                return matrixCursor
            }
            Location.CURRENT_POSITION_ID
        } else {
            "${matching.groups[1]!!.value}&${matching.groups[2]!!.value}&${matching.groups[3]!!.value}"
        }

        val location = runBlocking {
            getLocationRepository(context!!)
                .getLocation(locationId, withParameters = false)
                ?.copy(
                    weather = getWeatherRepository(context!!)
                        .getWeatherByLocationId(
                            locationId,
                            withDaily = !withDailyQuery.equals("false", ignoreCase = true),
                            withHourly = !withHourlyQuery.equals("false", ignoreCase = true),
                            withMinutely = !withMinutelyQuery.equals("false", ignoreCase = true),
                            withAlerts = !withAlertsQuery.equals("false", ignoreCase = true),
                            withNormals = !withNormalsQuery.equals("false", ignoreCase = true)
                        )
                )
        }
        if (location != null) {
            val pollenIndexSource = location.pollenSource?.let {
                getSourceManager(context!!).getPollenIndexSource(it)
            }
            matrixCursor.addRow(
                arrayOf<Any?>(
                    location.formattedId,
                    location.latitude,
                    location.longitude,
                    if (location.isCurrentPosition) 1 else 0,
                    location.timeZone.id,
                    location.customName,
                    location.country,
                    location.countryCode,
                    location.admin1,
                    location.admin1Code,
                    location.admin2,
                    location.admin2Code,
                    location.admin3,
                    location.admin3Code,
                    location.admin4,
                    location.admin4Code,
                    location.city,
                    location.district,
                    Json.encodeToString(
                        getWeatherData(
                            location,
                            pollenIndexSource,
                            temperatureUnitQuery,
                            precipitationUnitQuery,
                            speedUnitQuery,
                            distanceUnitQuery,
                            pressureUnitQuery
                        )
                    ).gzipCompress()
                )
            )
        }
        return matrixCursor
    }

    private fun getWeatherData(
        location: Location,
        pollenIndexSource: PollenIndexSource?,
        temperatureUnitQuery: String?,
        precipitationUnitQuery: String?,
        speedUnitQuery: String?,
        distanceUnitQuery: String?,
        pressureUnitQuery: String?,
    ): FreezyWeather? {
        val settings = SettingsManager.getInstance(context!!)
        val temperatureUnit = temperatureUnitQuery?.let { TemperatureUnit.getUnit(it) }
            ?: settings.getTemperatureUnit(context!!)
        val precipitationUnit = precipitationUnitQuery?.let { PrecipitationUnit.getUnit(it) }
            ?: settings.getPrecipitationUnit(context!!)
        val snowfallUnit = precipitationUnitQuery?.let { PrecipitationUnit.getUnit(it) }
            ?: settings.getPrecipitationUnit(context!!)
        val speedUnit = speedUnitQuery?.let { SpeedUnit.getUnit(it) }
            ?: settings.getSpeedUnit(context!!)
        val distanceUnit = distanceUnitQuery?.let { DistanceUnit.getUnit(it) }
            ?: settings.getDistanceUnit(context!!)
        val pressureUnit = pressureUnitQuery?.let { PressureUnit.getUnit(it) }
            ?: settings.getPressureUnit(context!!)

        return location.weather?.let { weather ->
            FreezyWeather(
                refreshTime = weather.base.refreshTime?.time,
                bulletin = FreezyBulletin(
                    weekly = weather.current?.dailyForecast,
                    nextHours = weather.current?.hourlyForecast,
                    nowcastingHeadline = weather.getMinutelyTitle(context!!),
                    nowcastingDescription = weather.getMinutelyDescription(context!!, location)
                ),
                current = getCurrent(
                    weather.current,
                    temperatureUnit,
                    speedUnit,
                    distanceUnit,
                    pressureUnit
                ),
                daily = getDaily(
                    weather.dailyForecast,
                    temperatureUnit,
                    precipitationUnit,
                    snowfallUnit,
                    speedUnit,
                    distanceUnit,
                    pressureUnit,
                    pollenIndexSource
                ),
                hourly = getHourly(
                    weather.hourlyForecast,
                    temperatureUnit,
                    precipitationUnit,
                    snowfallUnit,
                    speedUnit,
                    distanceUnit,
                    pressureUnit
                ),
                minutely = getMinutely(
                    weather.minutelyForecast,
                    precipitationUnit
                ),
                alerts = getAlerts(
                    weather.alertList
                ),
                normals = getNormals(
                    weather.normals,
                    temperatureUnit
                ),
                sources = getSources(location)
            )
        }
    }

    private fun getCurrent(
        current: Current?,
        temperatureUnit: TemperatureUnit,
        speedUnit: SpeedUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
    ): FreezyCurrent? {
        return current?.let { cur ->
            FreezyCurrent(
                weatherText = cur.weatherText,
                weatherCode = cur.weatherCode?.id,
                temperature = getTemperature(cur.temperature, temperatureUnit),
                wind = getWind(cur.wind, speedUnit),
                uV = getUV(cur.uV),
                airQuality = getAirQuality(cur.airQuality),
                relativeHumidity = getPercentUnit(cur.relativeHumidity),
                dewPoint = getTemperatureUnit(cur.dewPoint, temperatureUnit),
                pressure = getPressureUnit(cur.pressure, pressureUnit),
                cloudCover = getPercentUnit(cur.cloudCover, cur.cloudCover?.getCloudCoverDescription(context!!)),
                visibility = getDistanceUnit(cur.visibility, distanceUnit),
                ceiling = getDistanceUnit(cur.ceiling, distanceUnit)
            )
        }
    }

    private fun getDaily(
        daily: List<Daily>?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        snowfallUnit: PrecipitationUnit,
        speedUnit: SpeedUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
        pollenIndexSource: PollenIndexSource?,
    ): List<FreezyDaily>? {
        return daily?.map { day ->
            FreezyDaily(
                date = day.date.time,
                day = getHalfDay(
                    day.day,
                    temperatureUnit,
                    precipitationUnit,
                    snowfallUnit,
                    speedUnit
                ),
                night = getHalfDay(
                    day.night,
                    temperatureUnit,
                    precipitationUnit,
                    snowfallUnit,
                    speedUnit
                ),
                degreeDay = day.degreeDay?.let {
                    FreezyDegreeDay(
                        heating = getDegreeDayTemperatureUnit(
                            it.heating,
                            temperatureUnit
                        ),
                        cooling = getDegreeDayTemperatureUnit(
                            it.cooling,
                            temperatureUnit
                        )
                    )
                },
                /*sun = getAstro(day.sun),
                twilight = getAstro(day.twilight),
                moon = getAstro(day.moon),
                moonPhase = day.moonPhase?.let {
                    BreezyMoonPhase(
                        angle = it.angle,
                        description = it.getDescription(context!!)
                    )
                },*/
                airQuality = getAirQuality(day.airQuality),
                pollen = getPollen(day.pollen, pollenIndexSource),
                uV = getUV(day.uV),
                sunshineDuration = getDurationUnit(day.sunshineDuration),
                relativeHumidity = FreezyDailyUnit(
                    avg = getPercentUnit(day.relativeHumidity?.average),
                    max = getPercentUnit(day.relativeHumidity?.max),
                    min = getPercentUnit(day.relativeHumidity?.min),
                    summary = null
                ),
                dewPoint = FreezyDailyUnit(
                    avg = getTemperatureUnit(day.dewPoint?.average, temperatureUnit),
                    max = getTemperatureUnit(day.dewPoint?.max, temperatureUnit),
                    min = getTemperatureUnit(day.dewPoint?.min, temperatureUnit),
                    summary = null
                ),
                pressure = FreezyDailyUnit(
                    avg = getPressureUnit(day.pressure?.average, pressureUnit),
                    max = getPressureUnit(day.pressure?.max, pressureUnit),
                    min = getPressureUnit(day.pressure?.min, pressureUnit),
                    summary = null
                ),
                cloudCover = FreezyDailyUnit(
                    avg = getPercentUnit(
                        day.cloudCover?.average,
                        day.cloudCover?.average?.getCloudCoverDescription(context!!)
                    ),
                    max = getPercentUnit(
                        day.cloudCover?.max,
                        day.cloudCover?.max?.getCloudCoverDescription(context!!)
                    ),
                    min = getPercentUnit(
                        day.cloudCover?.min,
                        day.cloudCover?.min?.getCloudCoverDescription(context!!)
                    ),
                    summary = null
                ),
                visibility = FreezyDailyUnit(
                    avg = getDistanceUnit(day.visibility?.average, distanceUnit),
                    max = getDistanceUnit(day.visibility?.max, distanceUnit),
                    min = getDistanceUnit(day.visibility?.min, distanceUnit),
                    summary = null
                )
            )
        }
    }

    private fun getHourly(
        hourly: List<Hourly>?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        snowfallUnit: PrecipitationUnit,
        speedUnit: SpeedUnit,
        distanceUnit: DistanceUnit,
        pressureUnit: PressureUnit,
    ): List<FreezyHourly>? {
        return hourly?.map { hour ->
            FreezyHourly(
                date = hour.date.time,
                isDaylight = hour.isDaylight,
                weatherText = hour.weatherText,
                weatherCode = hour.weatherCode?.id,
                temperature = getTemperature(hour.temperature, temperatureUnit),
                precipitation = getPrecipitation(hour.precipitation, precipitationUnit, snowfallUnit),
                precipitationProbability = getPrecipitationProbability(hour.precipitationProbability),
                wind = getWind(hour.wind, speedUnit),
                airQuality = getAirQuality(hour.airQuality),
                uV = getUV(hour.uV),
                relativeHumidity = getPercentUnit(hour.relativeHumidity),
                dewPoint = getTemperatureUnit(hour.dewPoint, temperatureUnit),
                pressure = getPressureUnit(hour.pressure, pressureUnit),
                cloudCover = getPercentUnit(hour.cloudCover, hour.cloudCover?.getCloudCoverDescription(context!!)),
                visibility = getDistanceUnit(hour.visibility, distanceUnit)
            )
        }
    }

    private fun getMinutely(
        minutely: List<Minutely>?,
        precipitationUnit: PrecipitationUnit,
    ): List<FreezyMinutely>? {
        return minutely?.map { minute ->
            FreezyMinutely(
                date = minute.date.time,
                minuteInterval = minute.minuteInterval,
                precipitationIntensity = getPrecipitationUnit(
                    minute.precipitationIntensity,
                    precipitationUnit
                )
            )
        }
    }

    private fun getNormals(
        normals: Map<Month, Normals>?,
        temperatureUnit: TemperatureUnit,
    ): Map<Int, FreezyNormals>? {
        return normals?.entries?.associate {
            it.key.value to FreezyNormals(
                daytimeTemperature = getTemperatureUnit(it.value.daytimeTemperature, temperatureUnit),
                nighttimeTemperature = getTemperatureUnit(it.value.nighttimeTemperature, temperatureUnit)
            )
        }
    }

    private fun getSources(
        location: Location,
    ): Map<String, FreezySource>? {
        return mapOf(
            SourceFeature.FORECAST to location.forecastSource,
            SourceFeature.CURRENT to location.currentSource,
            SourceFeature.AIR_QUALITY to location.airQualitySource,
            SourceFeature.POLLEN to location.pollenSource,
            SourceFeature.MINUTELY to location.minutelySource,
            SourceFeature.ALERT to location.alertSource,
            SourceFeature.NORMALS to location.normalsSource,
            SourceFeature.REVERSE_GEOCODING to location.reverseGeocodingSource
        ).filter { !it.value.isNullOrEmpty() }.mapNotNull {
            getSourceManager(context!!).getFeatureSource(it.value!!)?.let { source ->
                if (source.supportedFeatures.containsKey(it.key)) {
                    it.key.id to FreezySource(
                        type = context!!.getString(it.key.resourceName),
                        text = source.supportedFeatures[it.key],
                        links = if (source is HttpSource) {
                            source.attributionLinks.filter { link ->
                                source.supportedFeatures[it.key]?.contains(link.key) == true
                            }
                        } else {
                            null
                        }
                    )
                } else {
                    null
                }
            }
        }.toMap()
    }

    /*private fun getAstro(astro: Astro?): BreezyAstro? {
        return astro?.let {
            BreezyAstro(
                riseDate = it.riseDate?.time,
                setDate = it.setDate?.time
            )
        }
    }*/

    private fun getHalfDay(
        halfDay: HalfDay?,
        temperatureUnit: TemperatureUnit,
        precipitationUnit: PrecipitationUnit,
        snowfallUnit: PrecipitationUnit,
        speedUnit: SpeedUnit,
    ): FreezyHalfDay? {
        return halfDay?.let { hd ->
            FreezyHalfDay(
                weatherCode = hd.weatherCode?.id,
                weatherText = hd.weatherText,
                weatherSummary = hd.weatherSummary,
                temperature = getTemperature(hd.temperature, temperatureUnit),
                precipitation = getPrecipitation(hd.precipitation, precipitationUnit, snowfallUnit),
                precipitationProbability = getPrecipitationProbability(hd.precipitationProbability),
                precipitationDuration = getPrecipitationDuration(hd.precipitationDuration),
                wind = getWind(hd.wind, speedUnit)
            )
        }
    }

    private fun getTemperature(
        temperature: freezyweather.domain.weather.model.Temperature?,
        temperatureUnit: TemperatureUnit,
    ): FreezyTemperature? {
        return temperature?.let {
            FreezyTemperature(
                temperature = getTemperatureUnit(it.temperature, temperatureUnit),
                sourceFeelsLike = getTemperatureUnit(it.sourceFeelsLike, temperatureUnit),
                computedApparent = getTemperatureUnit(it.computedApparent, temperatureUnit),
                computedWindChill = getTemperatureUnit(it.computedWindChill, temperatureUnit),
                computedHumidex = getTemperatureUnit(it.computedHumidex, temperatureUnit)
            )
        }
    }

    private fun getTemperatureUnit(
        temperature: Temperature?,
        temperatureUnit: TemperatureUnit,
    ): FreezyUnit? {
        return temperature?.let {
            FreezyUnit(
                value = it.toDouble(temperatureUnit).roundDecimals(temperatureUnit.decimals.long),
                unit = temperatureUnit.id
            )
        }
    }

    private fun getDegreeDayTemperatureUnit(
        temperature: Temperature?,
        temperatureUnit: TemperatureUnit,
    ): FreezyUnit? {
        return temperature?.let {
            FreezyUnit(
                value = it.toDoubleDeviation(temperatureUnit).roundDecimals(temperatureUnit.decimals.long),
                unit = temperatureUnit.id
            )
        }
    }

    private fun getPrecipitation(
        precipitation: freezyweather.domain.weather.model.Precipitation?,
        precipitationUnit: PrecipitationUnit,
        snowfallUnit: PrecipitationUnit,
    ): FreezyPrecipitation? {
        return precipitation?.let {
            FreezyPrecipitation(
                total = getPrecipitationUnit(it.total, precipitationUnit),
                thunderstorm = getPrecipitationUnit(it.thunderstorm, precipitationUnit),
                rain = getPrecipitationUnit(it.rain, precipitationUnit),
                snow = getPrecipitationUnit(it.snow, snowfallUnit),
                ice = getPrecipitationUnit(it.ice, precipitationUnit)
            )
        }
    }

    private fun getPrecipitationUnit(
        precipitation: Precipitation?,
        precipitationUnit: PrecipitationUnit,
    ): FreezyUnit? {
        return precipitation?.let {
            FreezyUnit(
                value = it.toDouble(precipitationUnit).roundDecimals(precipitationUnit.decimals.long),
                unit = precipitationUnit.id
            )
        }
    }

    private fun getPrecipitationProbability(
        precipitationProbability: PrecipitationProbability?,
    ): FreezyPrecipitationProbability? {
        return precipitationProbability?.let {
            FreezyPrecipitationProbability(
                total = getPercentUnit(it.total),
                thunderstorm = getPercentUnit(it.thunderstorm),
                rain = getPercentUnit(it.rain),
                snow = getPercentUnit(it.snow),
                ice = getPercentUnit(it.ice)
            )
        }
    }

    private fun getPrecipitationDuration(
        precipitationDuration: PrecipitationDuration?,
    ): FreezyPrecipitationDuration? {
        return precipitationDuration?.let {
            FreezyPrecipitationDuration(
                total = getDurationUnit(it.total),
                thunderstorm = getDurationUnit(it.thunderstorm),
                rain = getDurationUnit(it.rain),
                snow = getDurationUnit(it.snow),
                ice = getDurationUnit(it.ice)
            )
        }
    }

    private fun getWind(
        wind: Wind?,
        speedUnit: SpeedUnit,
    ): FreezyWind? {
        return wind?.let {
            FreezyWind(
                degree = wind.degree?.roundDecimals(1),
                speed = getSpeedUnit(wind.speed, speedUnit),
                gusts = getSpeedUnit(wind.gusts, speedUnit)
            )
        }
    }

    private fun getUV(
        uV: UV?,
    ): FreezyUnit? {
        return uV?.index?.let {
            FreezyUnit(
                value = it.roundDecimals(1),
                unit = "uvi",
                description = uV.getLevel(context!!),
                color = colorToHex(uV.getUVColor(context!!))
            )
        }
    }

    private fun getSpeedUnit(
        speed: Speed?,
        speedUnit: SpeedUnit,
    ): FreezyUnit? {
        return speed?.let {
            FreezyUnit(
                value = it.toDouble(speedUnit).roundDecimals(speedUnit.decimals.long),
                unit = speedUnit.id,
                description = it.getBeaufortScaleStrength(context!!),
                color = colorToHex(it.getBeaufortScaleColor(context!!))
            )
        }
    }

    private fun getDistanceUnit(
        distance: Distance?,
        distanceUnit: DistanceUnit,
    ): FreezyUnit? {
        return distance?.let {
            FreezyUnit(
                value = it.toDouble(distanceUnit).roundDecimals(distanceUnit.decimals.long),
                unit = distanceUnit.id,
                description = it.getVisibilityDescription(context!!)
            )
        }
    }

    private fun getPressureUnit(
        pressure: Pressure?,
        pressureUnit: PressureUnit,
    ): FreezyUnit? {
        return pressure?.let {
            FreezyUnit(
                value = it.toDouble(pressureUnit).roundDecimals(pressureUnit.decimals.long),
                unit = pressureUnit.id
            )
        }
    }

    private fun getPercentUnit(
        percent: Ratio?,
        description: String? = null,
    ): FreezyUnit? {
        return percent?.let {
            FreezyUnit(
                value = it.inPercent.roundDecimals(1),
                unit = "percent",
                description = description
            )
        }
    }

    private fun getDurationUnit(
        duration: Duration?,
    ): FreezyUnit? {
        return duration?.let {
            FreezyUnit(
                value = it.inWholeMinutes.toDouble(),
                unit = "m"
            )
        }
    }

    private fun getAirQuality(
        airQuality: AirQuality?,
    ): FreezyAirQuality? {
        return airQuality?.let {
            if (airQuality.isValid) {
                FreezyAirQuality(
                    index = FreezyUnit(
                        value = airQuality.getIndex()?.toDouble(),
                        unit = "aqi",
                        description = airQuality.getName(context!!),
                        color = colorToHex(airQuality.getColor(context!!))
                    ),
                    pollutants = airQuality.validPollutants.associate {
                        it.id to FreezyPollutant(
                            index = FreezyUnit(
                                value = airQuality.getIndex(it)?.toDouble(),
                                unit = "aqi",
                                description = airQuality.getName(context!!, it),
                                color = colorToHex(airQuality.getColor(context!!, it))
                            ),
                            concentration = FreezyUnit(
                                value = airQuality.getConcentration(it)?.roundDecimals(1),
                                unit = PollutantIndex.getUnit(it).id
                            )
                        )
                    }
                )
            } else {
                null
            }
        }
    }

    private fun getPollen(
        pollen: Pollen?,
        pollenIndexSource: PollenIndexSource?,
    ): Map<String, FreezyPollen>? {
        return pollen?.let {
            if (it.isValid) {
                it.validPollens.associate { component ->
                    component.id to FreezyPollen(
                        name = it.getName(context!!, component),
                        concentration = FreezyUnit(
                            value = if (pollenIndexSource == null) {
                                it.getConcentration(component)?.value?.toDouble()
                            } else {
                                null
                            },
                            unit = PollenConcentrationUnit.PER_CUBIC_METER.id,
                            description = it.getIndexName(context!!, component, pollenIndexSource),
                            color = colorToHex(it.getColor(context!!, component, pollenIndexSource))
                        )
                    )
                }
            } else {
                null
            }
        }
    }

    private fun getAlerts(alertList: List<Alert>?): List<FreezyAlert>? {
        return alertList?.map { alert ->
            FreezyAlert(
                alertId = alert.alertId,
                startDate = alert.startDate?.time,
                endDate = alert.endDate?.time,
                headline = alert.headline,
                description = alert.description,
                instruction = alert.instruction,
                source = alert.source,
                severity = alert.severity.id,
                color = colorToHex(alert.color)
            )
        }
    }

    private fun colorToHex(@ColorInt colorInt: Int): String {
        return ColorUtils.setAlphaComponent(colorInt, 0).toHexString(hexFormat)
    }

    override fun getType(uri: Uri): String? {
        // MIME types are not relevant (for now at least)
        return null
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri? {
        // This content provider is read-only for now, so we always return null
        return null
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        // This content provider is read-only for now, so we always return 0
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        // This content provider is read-only for now, so we always return 0
        return 0
    }

    companion object {
        private const val TAG = "Breezy"

        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.weather"

        private val uriMatcher = object : UriMatcher(NO_MATCH) {
            init {
                addURI(AUTHORITY, ProviderUri.VERSION_PATH, ProviderUri.VERSION_CODE)
                addURI(AUTHORITY, ProviderUri.LOCATIONS_PATH, ProviderUri.LOCATIONS_CODE)
                addURI(AUTHORITY, ProviderUri.WEATHER_PATH, ProviderUri.WEATHER_CODE)
            }
        }
    }
}
