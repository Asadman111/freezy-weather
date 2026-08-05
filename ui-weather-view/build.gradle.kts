import com.android.build.api.dsl.LibraryExtension

plugins {
    id("freezy.library")
}

configure<LibraryExtension> {
    namespace = "org.freezyweather.ui.theme.weatherView"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.core.ktx)
}
