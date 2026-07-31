import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
    id("template.android.hilt")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.bsp.wsiw.core.database"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(catalog.findLibrary("room-runtime").get())
    implementation(catalog.findLibrary("room-ktx").get())
    ksp(catalog.findLibrary("room-compiler").get())

    testImplementation(catalog.findLibrary("room-testing").get())
    testImplementation(catalog.findLibrary("junit").get())
    testImplementation(catalog.findLibrary("robolectric").get())
    testImplementation(catalog.findLibrary("coroutines-test").get())
    testImplementation(catalog.findLibrary("turbine").get())
}
