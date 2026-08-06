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
import freezyweather.domain.weather.wrappers.WeatherWrapper
import io.reactivex.rxjava3.core.Observable
import org.freezyweather.common.exceptions.NonFreeNetSourceException
import javax.inject.Inject

class CnemcService @Inject constructor(
    @ApplicationContext context: Context,
) : CnemcServiceStub(context) {

    override fun requestWeather(
        context: Context,
        location: Location,
        requestedFeatures: List<SourceFeature>,
    ): Observable<WeatherWrapper> {
        throw NonFreeNetSourceException()
    }

    override fun needsLocationParametersRefresh(
        location: Location,
        coordinatesChanged: Boolean,
        features: List<SourceFeature>,
    ): Boolean = false

    override fun requestLocationParameters(
        context: Context,
        location: Location,
    ): Observable<Map<String, String>> {
        throw NonFreeNetSourceException()
    }
}
