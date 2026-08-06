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
import freezyweather.domain.location.model.Location
import freezyweather.domain.source.SourceContinent
import freezyweather.domain.source.SourceFeature
import org.freezyweather.common.source.HttpSource
import org.freezyweather.common.source.LocationParametersSource
import org.freezyweather.common.source.NonFreeNetSource
import org.freezyweather.common.source.WeatherSource
import org.freezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.freezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE

abstract class CnemcServiceStub(context: Context) :
    HttpSource(),
    WeatherSource,
    LocationParametersSource,
    NonFreeNetSource {

    override val id = "cnemc"
    override val name = "中国环境监测总站 (CNEMC)"
    override val continent = SourceContinent.ASIA

    override val privacyPolicyUrl = "https://www.cnemc.cn/"

    protected val airQualityAttribution = "中国环境监测总站 (CNEMC)"
    override val supportedFeatures = mapOf(
        SourceFeature.AIR_QUALITY to airQualityAttribution
    )
    override val attributionLinks = mapOf(
        "中国环境监测总站" to "https://www.cnemc.cn/"
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return feature == SourceFeature.AIR_QUALITY &&
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
}
