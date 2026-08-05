
import freezy.buildlogic.configureCompose
import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("com.android.application")

    id("freezy.code.lint")
}

configure<ApplicationExtension> {
    configureCompose()
}
