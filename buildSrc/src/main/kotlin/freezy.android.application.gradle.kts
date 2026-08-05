
import freezy.buildlogic.AndroidConfig
import freezy.buildlogic.configureAndroidApplication
import freezy.buildlogic.configureTest
import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")

    id("freezy.code.lint")
}

configure<ApplicationExtension> {
    defaultConfig {
        targetSdk = AndroidConfig.TARGET_SDK
    }
    configureAndroidApplication()
    configureTest()
}
