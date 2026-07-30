import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.compose")
    id("template.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.roborazzi)
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.bsp.wsiw.feature.home"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(project(":layer:layer-domain"))

    implementation(catalog.findLibrary("androidx-lifecycle-viewmodel").get())
    implementation(catalog.findLibrary("androidx-lifecycle-runtime-compose").get())
    implementation(catalog.findLibrary("androidx-navigation3-runtime").get())
    implementation(catalog.findLibrary("hilt-navigation-compose").get())
    implementation(catalog.findLibrary("kotlinx-serialization-core").get())

    testImplementation(project(":core:core-testing"))
    testImplementation(catalog.findLibrary("robolectric").get())
    testImplementation(catalog.findLibrary("roborazzi").get())
    testImplementation(catalog.findLibrary("roborazzi-compose").get())
    testImplementation(catalog.findLibrary("roborazzi-rule").get())
    testImplementation(platform(catalog.findLibrary("androidx-compose-bom").get()))
    testImplementation(catalog.findLibrary("androidx-compose-ui-test-junit4").get())
}
