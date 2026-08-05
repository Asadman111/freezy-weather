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

package freezy.buildlogic

import org.gradle.api.Project

interface BuildConfig {
    // You’re NOT allowed to redistribute modified APKs with the brand config enabled, see license terms
    val isFreezy: Boolean
}

val Project.Config: BuildConfig get() = object : BuildConfig {
    override val isFreezy: Boolean = project.hasProperty("breezy")
}
