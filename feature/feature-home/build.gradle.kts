import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.compose")
    id("template.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.template.android.feature.home"
}

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))

    implementation(catalog.findLibrary("androidx-lifecycle-viewmodel").get())
    implementation(catalog.findLibrary("androidx-lifecycle-runtime-compose").get())
    implementation(catalog.findLibrary("androidx-navigation3-runtime").get())
    implementation(catalog.findLibrary("hilt-navigation-compose").get())
    implementation(catalog.findLibrary("kotlinx-serialization-core").get())
}
