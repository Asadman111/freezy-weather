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

package org.breezyweather.option.appearance

import android.content.Context
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.breezyweather.R
import org.breezyweather.common.options.appearance.AppFontFamily
import org.breezyweather.common.options.appearance.AppFontSize
import org.junit.jupiter.api.Test

class AppFontOptionsTest {

    @Test
    fun appFontFamilyGetInstance() = runTest {
        AppFontFamily.getInstance("system") shouldBe AppFontFamily.SYSTEM
        AppFontFamily.getInstance("serif") shouldBe AppFontFamily.SERIF
        AppFontFamily.getInstance("monospace") shouldBe AppFontFamily.MONOSPACE
        AppFontFamily.getInstance("cursive") shouldBe AppFontFamily.CURSIVE
        AppFontFamily.getInstance("unknown") shouldBe AppFontFamily.SYSTEM
    }

    @Test
    fun appFontFamilyProperties() = runTest {
        AppFontFamily.SYSTEM.fontFamily.shouldNotBeNull()
        AppFontFamily.SERIF.fontFamily.shouldNotBeNull()
        AppFontFamily.MONOSPACE.fontFamily.shouldNotBeNull()
        AppFontFamily.CURSIVE.fontFamily.shouldNotBeNull()

        AppFontFamily.SYSTEM.genericFamily shouldBe null
        AppFontFamily.SERIF.genericFamily shouldBe "serif"
        AppFontFamily.MONOSPACE.genericFamily shouldBe "monospace"
        AppFontFamily.CURSIVE.genericFamily shouldBe "cursive"
    }

    @Test
    fun appFontFamilyGetName() = runTest {
        val context = mockk<Context>().apply {
            every { resources } returns mockk {
                every { getStringArray(R.array.app_font_families) } returns
                    arrayOf("System default", "Serif", "Monospace", "Cursive")
                every { getStringArray(R.array.app_font_family_values) } returns
                    arrayOf("system", "serif", "monospace", "cursive")
            }
        }
        AppFontFamily.SYSTEM.getName(context) shouldBe "System default"
        AppFontFamily.SERIF.getName(context) shouldBe "Serif"
    }

    @Test
    fun appFontSizeGetInstance() = runTest {
        AppFontSize.getInstance("small") shouldBe AppFontSize.SMALL
        AppFontSize.getInstance("default") shouldBe AppFontSize.DEFAULT
        AppFontSize.getInstance("large") shouldBe AppFontSize.LARGE
        AppFontSize.getInstance("extra_large") shouldBe AppFontSize.EXTRA_LARGE
        AppFontSize.getInstance("unknown") shouldBe AppFontSize.DEFAULT
    }

    @Test
    fun appFontSizeScale() = runTest {
        AppFontSize.SMALL.scale shouldBe 0.85f
        AppFontSize.DEFAULT.scale shouldBe 1f
        AppFontSize.LARGE.scale shouldBe 1.15f
        AppFontSize.EXTRA_LARGE.scale shouldBe 1.3f
    }

    @Test
    fun appFontSizeGetName() = runTest {
        val context = mockk<Context>().apply {
            every { resources } returns mockk {
                every { getStringArray(R.array.app_font_sizes) } returns
                    arrayOf("Small", "Default", "Large", "Extra large")
                every { getStringArray(R.array.app_font_size_values) } returns
                    arrayOf("small", "default", "large", "extra_large")
            }
        }
        AppFontSize.DEFAULT.getName(context) shouldBe "Default"
        AppFontSize.LARGE.getName(context) shouldBe "Large"
    }
}
