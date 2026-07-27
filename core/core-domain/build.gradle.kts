import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.bsp.wsiw.core.domain"
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(catalog.findLibrary("coroutines-android").get())
    // JSR-330 annotation only — Hilt reads @Inject at compile time; no runtime dep needed
    compileOnly("javax.inject:javax.inject:1")
}
