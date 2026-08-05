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

package org.freezyweather.sources.bmd

import android.content.Context
import freezyweather.domain.location.model.Location
import freezyweather.domain.source.SourceContinent
import freezyweather.domain.source.SourceFeature
import org.freezyweather.common.extensions.code
import org.freezyweather.common.extensions.currentLocale
import org.freezyweather.common.extensions.getCountryName
import org.freezyweather.common.source.HttpSource
import org.freezyweather.common.source.LocationParametersSource
import org.freezyweather.common.source.NonFreeNetSource
import org.freezyweather.common.source.ReverseGeocodingSource
import org.freezyweather.common.source.WeatherSource
import org.freezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.freezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class BmdServiceStub(context: Context) :
    HttpSource(),
    WeatherSource,
    ReverseGeocodingSource,
    LocationParametersSource,
    NonFreeNetSource {

    override val id = "bmd"
    override val name by lazy {
        if (context.currentLocale.code.startsWith("bn")) {
            "বাংলাদেশ আবহাওয়া অধিদপ্তর"
        } else {
            "BMD (${context.currentLocale.getCountryName("BD")})"
        }
    }
    override val continent = SourceContinent.ASIA
    override val privacyPolicyUrl = ""

    protected val weatherAttribution = if (context.currentLocale.code.startsWith("bn")) {
        "বাংলাদেশ আবহাওয়া অধিদপ্তর"
    } else {
        "Bangladesh Meteorological Department"
    }
    override val supportedFeatures = mapOf(
        SourceFeature.FORECAST to weatherAttribution,
        SourceFeature.REVERSE_GEOCODING to weatherAttribution
    )

    override fun isFeatureSupportedForLocation(
        location: Location,
        feature: SourceFeature,
    ): Boolean {
        return location.countryCode.equals("BD", ignoreCase = true)
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

    // Only supports its own country
    override val knownAmbiguousCountryCodes: Array<String>? = null
}
