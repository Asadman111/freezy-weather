import com.android.build.api.dsl.LibraryExtension

plugins {
    id("freezy.library")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
}

configure<LibraryExtension> {
    namespace = "freezyweather.data"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    sqldelight {
        databases {
            create("Database") {
                packageName.set("freezyweather.data")
                dialect(libs.sqldelight.dialects.sql)
                schemaOutputDirectory.set(project.file("./src/main/sqldelight"))
            }
        }
    }
}

dependencies {
    implementation(projects.domain)
    implementation(projects.weatherUnit)

    api(libs.bundles.sqldelight)
}
