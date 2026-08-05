import freezy.buildlogic.configureAndroidLibrary
import freezy.buildlogic.configureTest
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("com.android.library")

    id("freezy.code.lint")
}

configure<LibraryExtension> {
    configureAndroidLibrary()
    configureTest()
}
