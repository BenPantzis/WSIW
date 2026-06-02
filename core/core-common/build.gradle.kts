import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.hilt")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.template.android.core.common"
}

dependencies {
    implementation(catalog.findLibrary("coroutines-android").get())
}
