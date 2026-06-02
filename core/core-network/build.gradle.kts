import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.hilt")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.template.android.core.network"
}

dependencies {
    implementation(catalog.findLibrary("retrofit-core").get())
    implementation(catalog.findLibrary("retrofit-gson").get())
    implementation(catalog.findLibrary("okhttp-core").get())
    implementation(catalog.findLibrary("okhttp-logging").get())
}
