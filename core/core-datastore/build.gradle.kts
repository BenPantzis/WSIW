import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.hilt")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.template.android.core.datastore"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(catalog.findLibrary("datastore-preferences").get())
    implementation(catalog.findLibrary("coroutines-android").get())
}
