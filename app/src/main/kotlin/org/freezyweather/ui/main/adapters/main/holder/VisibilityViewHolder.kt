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

package org.freezyweather.ui.main.adapters.main.holder

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import freezyweather.domain.location.model.Location
import org.freezyweather.R
import org.freezyweather.common.activities.FreezyActivity
import org.freezyweather.common.extensions.formatMeasure
import org.freezyweather.common.extensions.getVisibilityDescription
import org.freezyweather.common.options.appearance.DetailScreen
import org.freezyweather.common.utils.UnitUtils
import org.freezyweather.common.utils.helpers.IntentHelper
import org.freezyweather.ui.theme.resource.providers.ResourceProvider
import org.freezyweather.unit.formatting.UnitWidth

class VisibilityViewHolder(parent: ViewGroup) : AbstractMainCardViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.container_main_visibility, parent, false)
) {
    private val visibilityValueView: TextView = itemView.findViewById(R.id.visibility_value)
    private val visibilityDescriptionView: TextView = itemView.findViewById(R.id.visibility_description)

    override fun onBindView(
        activity: FreezyActivity,
        location: Location,
        provider: ResourceProvider,
        listAnimationEnabled: Boolean,
        itemAnimationEnabled: Boolean,
    ) {
        super.onBindView(activity, location, provider, listAnimationEnabled, itemAnimationEnabled)

        val talkBackBuilder = StringBuilder(context.getString(R.string.visibility))

        location.weather!!.current?.visibility?.let { visibility ->
            visibilityValueView.text = UnitUtils.formatUnitsHalfSize(
                visibility.formatMeasure(context)
            )
            visibilityDescriptionView.text = visibility.getVisibilityDescription(context)

            talkBackBuilder.append(context.getString(R.string.colon_separator))
            talkBackBuilder.append(visibility.formatMeasure(context, unitWidth = UnitWidth.LONG))
            talkBackBuilder.append(context.getString(org.freezyweather.unit.R.string.locale_separator))
            talkBackBuilder.append(visibilityValueView.text)
        }

        itemView.contentDescription = talkBackBuilder.toString()
        itemView.setOnClickListener {
            IntentHelper.startDailyWeatherActivity(
                context as FreezyActivity,
                location.formattedId,
                location.weather!!.todayIndex,
                DetailScreen.TAG_VISIBILITY
            )
        }
    }
}
