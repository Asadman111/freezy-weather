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

package org.breezyweather.common.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import org.breezyweather.BreezyWeather
import org.breezyweather.R
import org.breezyweather.common.extensions.isDarkMode
import org.breezyweather.common.extensions.setSystemBarStyle
import org.breezyweather.common.options.appearance.AppFontFamily
import org.breezyweather.common.snackbar.SnackbarContainer
import org.breezyweather.domain.settings.SettingsManager

abstract class BreezyActivity : AppCompatActivity() {

    private var fontConfigKey: String? = null

    override fun attachBaseContext(newBase: Context) {
        val settingsManager = SettingsManager.getInstance(BreezyWeather.instance)
        val overlayStyleId = when (settingsManager.appFontFamily) {
            AppFontFamily.SERIF -> R.style.FontFamilyOverlay_Serif
            AppFontFamily.MONOSPACE -> R.style.FontFamilyOverlay_Monospace
            AppFontFamily.CURSIVE -> R.style.FontFamilyOverlay_Cursive
            AppFontFamily.SYSTEM -> null
        }
        val themedBase = if (overlayStyleId == null) {
            newBase
        } else {
            ContextThemeWrapper(newBase, overlayStyleId)
        }

        // Apply the app font size multiplier on top of the system font scale.
        val configuration = Configuration(themedBase.resources.configuration)
        configuration.fontScale = themedBase.resources.configuration.fontScale * settingsManager.appFontSize.scale

        super.attachBaseContext(themedBase.createConfigurationContext(configuration))
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            window.setSystemBarStyle(!isDarkMode)
        }

        BreezyWeather.instance.addActivity(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        BreezyWeather.instance.setTopActivity(this)
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        BreezyWeather.instance.setTopActivity(this)

        // Recreate the activity if the app font settings changed while it was paused,
        // so the new font family/font size is applied to the View-based UI.
        val currentFontConfigKey = SettingsManager.getInstance(this).let {
            it.appFontFamily.id + ":" + it.appFontSize.id
        }
        if (fontConfigKey != null && fontConfigKey != currentFontConfigKey) {
            fontConfigKey = currentFontConfigKey
            recreate()
        } else {
            fontConfigKey = currentFontConfigKey
        }
    }

    @CallSuper
    override fun onPause() {
        super.onPause()
        BreezyWeather.instance.checkToCleanTopActivity(this)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        BreezyWeather.instance.removeActivity(this)
    }

    fun updateLocalNightMode(expectedLightTheme: Boolean) {
        getDelegate().localNightMode = if (expectedLightTheme) {
            AppCompatDelegate.MODE_NIGHT_NO
        } else {
            AppCompatDelegate.MODE_NIGHT_YES
        }
    }

    open val snackbarContainer: SnackbarContainer
        get() = SnackbarContainer(
            this,
            findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as ViewGroup,
            true
        )

    fun provideSnackbarContainer(): SnackbarContainer = snackbarContainer

    val isActivityCreated: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    val isActivityStarted: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    val isActivityResumed: Boolean
        get() = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
}
