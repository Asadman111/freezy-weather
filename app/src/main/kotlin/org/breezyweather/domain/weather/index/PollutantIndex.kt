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

package org.breezyweather.domain.weather.index

import android.content.Context
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import org.breezyweather.R
import org.breezyweather.common.extensions.currentLocale
import org.breezyweather.common.options.AirQualityIndexType
import org.breezyweather.domain.settings.SettingsManager
import org.breezyweather.unit.formatting.UnitWidth
import org.breezyweather.unit.pollutant.PollutantConcentrationUnit
import org.breezyweather.unit.precipitation.PrecipitationUnit
import kotlin.math.max
import kotlin.math.roundToInt

enum class PollutantIndex(
    val id: String,
    val thresholds: List<Int>,
    val maxY: Int,
    val molecularMass: Double?,
    @StringRes val shortName: Int,
    @StringRes val voicedName: Int,
    @StringRes val fullName: Int,
    @StringRes val aboutPollutant: Int,
    @StringRes val aboutIndex: Int,
    @StringRes val sources: Int,
) {
    PM25(
        "pm25",
        listOf(0, 5, 15, 30, 60, 150), // Plume 2023
        60,
        null,
        R.string.air_quality_pm25,
        R.string.air_quality_pm25_voice,
        R.string.air_quality_pm25_full,
        R.string.air_quality_pm25_about,
        R.string.air_quality_pm25_index,
        R.string.air_quality_pm_sources
    ),
    PM10(
        "pm10",
        listOf(0, 15, 45, 80, 160, 400), // Plume 2023
        160,
        null,
        R.string.air_quality_pm10,
        R.string.air_quality_pm10_voice,
        R.string.air_quality_pm10_full,
        R.string.air_quality_pm10_about,
        R.string.air_quality_pm10_index,
        R.string.air_quality_pm_sources
    ),
    O3(
        "o3",
        listOf(0, 50, 100, 160, 240, 480), // Plume 2023
        240,
        48.0,
        R.string.air_quality_o3,
        R.string.air_quality_o3_voice,
        R.string.air_quality_o3_full,
        R.string.air_quality_o3_about,
        R.string.air_quality_o3_index,
        R.string.air_quality_o3_sources
    ),
    NO2(
        "no2",
        listOf(0, 10, 25, 200, 400, 1000), // Plume 2023
        200,
        46.0055,
        R.string.air_quality_no2,
        R.string.air_quality_no2_voice,
        R.string.air_quality_no2_full,
        R.string.air_quality_no2_about,
        R.string.air_quality_no2_index,
        R.string.air_quality_no2_sources
    ),
    SO2(
        "so2",
        listOf(
            0,
            20,
            40, // daily
            270,
            500, // 10 min
            960 // linear prolongation
        ), // WHO 2021
        270,
        64.066,
        R.string.air_quality_so2,
        R.string.air_quality_so2_voice,
        R.string.air_quality_so2_full,
        R.string.air_quality_so2_about,
        R.string.air_quality_so2_index,
        R.string.air_quality_so2_sources
    ),
    CO(
        "co",
        listOf(
            0,
            2,
            4, // daily
            35, // hourly
            100, // 15 min
            230 // linear prolongation
        ), // WHO 2021
        35,
        28.01,
        R.string.air_quality_co,
        R.string.air_quality_co_voice,
        R.string.air_quality_co_full,
        R.string.air_quality_co_about,
        R.string.air_quality_co_index,
        R.string.air_quality_co_sources
    ),
    ;

    companion object {
        // Plume 2023
        val aqiThresholds = listOf(0, 20, 50, 100, 150, 250)
        val namesArrayId = R.array.air_quality_levels
        val descriptionsArrayId = R.array.air_quality_level_descriptions
        val harmlessExposuresArrayId = R.array.air_quality_level_harmless_exposures
        val colorsArrayId = R.array.air_quality_level_colors

        val indexFreshAir = aqiThresholds[1]
        val indexHighPollution = aqiThresholds[3]
        val indexExcessivePollution = aqiThresholds.last()

        // China AQI (HJ 633-2012)
        val chinaAqiThresholds = listOf(0, 50, 100, 150, 200, 300, 400, 500)
        val chinaLevelThresholds = listOf(0, 50, 100, 150, 200, 300)
        private val chinaPollutantThresholds = mapOf(
            PM25 to listOf(0, 35, 75, 115, 150, 250, 350, 500),
            PM10 to listOf(0, 50, 150, 250, 350, 420, 500, 600),
            SO2 to listOf(0, 50, 150, 475, 800, 1600, 2100, 2620),
            NO2 to listOf(0, 40, 80, 180, 280, 565, 750, 940),
            CO to listOf(0, 2, 4, 14, 24, 36, 48, 60)
        )
        val chinaO3_1hThresholds = listOf(0, 160, 200, 300, 400, 800, 1000, 1200)
        val chinaO3_8hThresholds = listOf(0, 100, 160, 215, 265, 800)

        val chinaNamesArrayId = R.array.air_quality_china_levels
        val chinaDescriptionsArrayId = R.array.air_quality_china_level_descriptions
        val chinaHarmlessExposuresArrayId = R.array.air_quality_china_level_harmless_exposures
        val chinaColorsArrayId = R.array.air_quality_china_level_colors

        fun getLevelThresholds(type: AirQualityIndexType): List<Int> {
            return if (type == AirQualityIndexType.CHINA) chinaLevelThresholds else aqiThresholds
        }

        /**
         * Thresholds used to display the scale/legend of a specific pollutant.
         * For the China standard, only the first 6 breakpoints are kept so the scale
         * matches the 6 air quality levels.
         */
        fun getLegendThresholds(
            pollutantIndex: PollutantIndex,
            type: AirQualityIndexType,
        ): List<Int> {
            return when (type) {
                AirQualityIndexType.CHINA -> when (pollutantIndex) {
                    O3 -> chinaO3_8hThresholds
                    else -> chinaPollutantThresholds[pollutantIndex]!!.take(6)
                }
                AirQualityIndexType.INTERNATIONAL -> pollutantIndex.thresholds
            }
        }

        fun getIndexFreshAir(type: AirQualityIndexType): Int = getLevelThresholds(type)[1]
        fun getIndexHighPollution(type: AirQualityIndexType): Int = getLevelThresholds(type)[3]
        fun getIndexExcessivePollution(type: AirQualityIndexType): Int = getLevelThresholds(type).last()
        fun getChartMaxIndex(type: AirQualityIndexType): Int = getLevelThresholds(type)[4]

        fun getAqiToLevel(
            aqi: Int?,
            type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
        ): Int? {
            if (aqi == null) return null
            val level = getLevelThresholds(type).indexOfLast { aqi >= it }
            return if (level >= 0) level else null
        }

        @ColorInt
        fun getAqiToColor(
            context: Context,
            aqi: Int?,
            type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
        ): Int {
            if (aqi == null) return Color.TRANSPARENT
            val level = getAqiToLevel(aqi, type)
            return if (level != null) {
                context.resources.getIntArray(
                    if (type == AirQualityIndexType.CHINA) chinaColorsArrayId else colorsArrayId
                ).getOrNull(level) ?: Color.TRANSPARENT
            } else {
                Color.TRANSPARENT
            }
        }

        fun getAqiToName(
            context: Context,
            aqi: Int?,
            type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
        ): String? {
            if (aqi == null) return null
            val level = getAqiToLevel(aqi, type)
            return if (level != null) {
                context.resources.getStringArray(
                    if (type == AirQualityIndexType.CHINA) chinaNamesArrayId else namesArrayId
                ).getOrNull(level)
            } else {
                null
            }
        }

        fun getAqiToDescription(
            context: Context,
            aqi: Int?,
            type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
        ): String? {
            if (aqi == null) return null
            val level = getAqiToLevel(aqi, type)
            return if (level != null) {
                context.resources.getStringArray(
                    if (type == AirQualityIndexType.CHINA) chinaDescriptionsArrayId else descriptionsArrayId
                ).getOrNull(level)
            } else {
                null
            }
        }

        fun getAqiToHarmlessExposure(
            context: Context,
            aqi: Int?,
            type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
        ): String? {
            if (aqi == null) return null
            val level = getAqiToLevel(aqi, type)
            return if (level != null) {
                context.resources.getStringArray(
                    if (type == AirQualityIndexType.CHINA) chinaHarmlessExposuresArrayId else harmlessExposuresArrayId
                ).getOrNull(level)
            } else {
                null
            }
        }

        fun getUnit(pollutantIndex: PollutantIndex): PollutantConcentrationUnit {
            return if (pollutantIndex == CO) {
                PollutantConcentrationUnit.MILLIGRAM_PER_CUBIC_METER
            } else {
                PollutantConcentrationUnit.MICROGRAM_PER_CUBIC_METER
            }
        }
    }

    private fun getIndex(cp: Double, bpLo: Int, bpHi: Int, inLo: Int, inHi: Int): Int {
        // Result will be incorrect if we don’t cast to double
        return (
            (inHi.toDouble() - inLo.toDouble()) /
                (bpHi.toDouble() - bpLo.toDouble()) *
                (cp - bpLo.toDouble()) +
                inLo.toDouble()
            ).roundToInt()
    }

    private fun getIndex(cp: Double, level: Int): Int {
        return if (level < thresholds.lastIndex) {
            getIndex(
                cp,
                thresholds[level],
                thresholds[level + 1],
                aqiThresholds[level],
                aqiThresholds[level + 1]
            )
        } else {
            // Continue producing a linear index above lastIndex
            ((cp * aqiThresholds.last()) / thresholds.last()).roundToInt()
        }
    }

    private fun getChinaIndex(cp: Double, breakpoints: List<Int>, indexLevels: List<Int>): Int {
        val level = breakpoints.indexOfLast { cp >= it }
        if (level < 0) return 0
        return if (level < indexLevels.lastIndex) {
            getIndex(
                cp,
                breakpoints[level],
                breakpoints[level + 1],
                indexLevels[level],
                indexLevels[level + 1]
            )
        } else {
            // Concentrations above the last breakpoint are capped at the maximum index
            indexLevels.last()
        }
    }

    fun getFullName(context: Context): String {
        return context.getString(
            fullName,
            if (this == PM10 || this == PM25) { // Cheating a little by using the precipitation unit
                PrecipitationUnit.MICROMETER.format(
                    context = context,
                    value = if (this == PM10) 10.0 else 2.5,
                    valueWidth = UnitWidth.LONG,
                    locale = context.currentLocale,
                    useNumberFormatter = SettingsManager.getInstance(context).useNumberFormatter,
                    useMeasureFormat = SettingsManager.getInstance(context).useMeasureFormat
                )
            } else {
                context.getString(shortName)
            }
        )
    }

    fun getIndex(
        cp: Double?,
        type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
    ): Int? {
        if (cp == null) return null
        return when (type) {
            AirQualityIndexType.INTERNATIONAL -> {
                val level = thresholds.indexOfLast { cp >= it }
                if (level >= 0) getIndex(cp, level) else null
            }
            AirQualityIndexType.CHINA -> {
                if (this == O3) {
                    max(
                        getChinaIndex(cp, chinaO3_1hThresholds, chinaAqiThresholds),
                        getChinaIndex(cp, chinaO3_8hThresholds, chinaAqiThresholds.take(chinaO3_8hThresholds.size))
                    )
                } else {
                    getChinaIndex(cp, chinaPollutantThresholds[this]!!, chinaAqiThresholds)
                }
            }
        }
    }

    fun getLevel(
        cp: Double?,
        type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
    ): Int? {
        if (cp == null) return null
        return when (type) {
            AirQualityIndexType.INTERNATIONAL -> {
                val level = thresholds.indexOfLast { cp >= it }
                if (level >= 0) level else null
            }
            AirQualityIndexType.CHINA -> {
                val breakpoints = chinaPollutantThresholds[this] ?: chinaO3_8hThresholds
                val level = breakpoints.indexOfLast { cp >= it }
                if (level >= 0) level else null
            }
        }
    }

    val excessivePollution = thresholds.last()

    fun getName(
        context: Context,
        cp: Double?,
        type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
    ): String? = getAqiToName(context, getIndex(cp, type), type)

    fun getDescription(
        context: Context,
        cp: Double?,
        type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
    ): String? = getAqiToDescription(context, getIndex(cp, type), type)

    @ColorInt
    fun getColor(
        context: Context,
        cp: Double?,
        type: AirQualityIndexType = AirQualityIndexType.INTERNATIONAL,
    ): Int = getAqiToColor(context, getIndex(cp, type), type)
}
