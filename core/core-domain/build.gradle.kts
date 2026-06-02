import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.template.android.core.domain"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(catalog.findLibrary("coroutines-android").get())
}
