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

package org.breezyweather.common.options

import android.content.Context
import org.breezyweather.R
import org.breezyweather.common.utils.UnitUtils

/**
 * Standard used to compute/interpret the Air Quality Index.
 *
 * - INTERNATIONAL: Plume AQI 2023 (default, used worldwide)
 * - CHINA: China HJ 633-2012 AQI
 */
enum class AirQualityIndexType(
    override val id: String,
) : BaseEnum {
    INTERNATIONAL("international"),
    CHINA("china"),
    ;

    override val valueArrayId = R.array.air_quality_index_type_values
    override val nameArrayId = R.array.air_quality_index_type_names

    override fun getName(context: Context) = UnitUtils.getName(context, this)

    companion object {

        fun getInstance(
            value: String?,
        ) = entries.firstOrNull {
            it.id == value
        } ?: INTERNATIONAL
    }
}
