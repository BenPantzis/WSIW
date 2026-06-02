import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.compose")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.template.android.core.ui"
}

dependencies {
    api(catalog.findLibrary("androidx-lifecycle-viewmodel").get())
    api(catalog.findLibrary("coroutines-android").get())

    api(platform(catalog.findLibrary("androidx-compose-bom").get()))
    api(catalog.findLibrary("androidx-compose-ui-core").get())
    api(catalog.findLibrary("androidx-compose-ui-graphics").get())
    api(catalog.findLibrary("androidx-compose-ui-preview").get())
    api(catalog.findLibrary("androidx-compose-material3").get())
    debugImplementation(catalog.findLibrary("androidx-compose-ui-tooling").get())

    implementation(catalog.findLibrary("coil-compose").get())
}
