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

package org.freezyweather.sources.ncei

import freezyweather.domain.location.model.Location
import freezyweather.domain.source.SourceContinent
import freezyweather.domain.source.SourceFeature
import org.freezyweather.common.source.HttpSource
import org.freezyweather.common.source.LocationParametersSource
import org.freezyweather.common.source.NonFreeNetSource
import org.freezyweather.common.source.WeatherSource
import org.freezyweather.common.source.WeatherSource.Companion.PRIORITY_HIGHEST
import org.freezyweather.common.source.WeatherSource.Companion.PRIORITY_NONE

/**
 * The actual implementation is in the src_freenet and src_nonfreenet folders
 */
abstract class NceiServiceStub() :
    HttpSource(),
    WeatherSource,
    LocationParametersSource,
    NonFreeNetSource {

    override val id = "ncei"
    override val name = "NCEI"
    override val continent = SourceContinent.WORLDWIDE

    protected val weatherAttribution = "National Centers for Environmental Information"
    override val supportedFeatures = mapOf(
        SourceFeature.NORMALS to weatherAttribution
    )

    override fun getFeaturePriorityForLocation(
        location: Location,
        feature: SourceFeature,
    ): Int {
        return when {
            arrayOf("US", "PR", "VI", "MP", "GU").any {
                location.countryCode.equals(it, ignoreCase = true)
            } -> PRIORITY_HIGHEST
            else -> PRIORITY_NONE
        }
    }
}
