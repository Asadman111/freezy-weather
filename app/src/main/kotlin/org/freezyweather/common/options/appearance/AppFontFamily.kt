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

package org.breezyweather.common.options.appearance

import android.content.Context
import androidx.compose.ui.text.font.FontFamily
import org.breezyweather.R
import org.breezyweather.common.options.BaseEnum
import org.breezyweather.common.utils.UnitUtils

enum class AppFontFamily(
    override val id: String,
    val fontFamily: FontFamily,
    /**
     * Android generic font family name, used to apply the font to the View-based UI
     * through a theme overlay. Null means "follow the system default".
     */
    val genericFamily: String?,
) : BaseEnum {

    SYSTEM("system", FontFamily.Default, null),
    SERIF("serif", FontFamily.Serif, "serif"),
    MONOSPACE("monospace", FontFamily.Monospace, "monospace"),
    CURSIVE("cursive", FontFamily.Cursive, "cursive"),
    ;

    companion object {

        fun getInstance(
            value: String,
        ) = entries.firstOrNull {
            it.id == value
        } ?: SYSTEM
    }

    override val valueArrayId = R.array.app_font_family_values
    override val nameArrayId = R.array.app_font_families

    override fun getName(context: Context) = UnitUtils.getName(context, this)
}
