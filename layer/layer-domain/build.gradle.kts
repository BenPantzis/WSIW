import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.bsp.wsiw.core.domain"
}

dependencies {
    api(project(":core:core-common"))
    api(catalog.findLibrary("coroutines-android").get())
    // JSR-330 annotation only — Hilt reads @Inject at compile time; no runtime dep needed
    compileOnly("javax.inject:javax.inject:1")
    // @Stable/@Immutable stability annotations for Compose compiler — compile-only, no runtime dep
    compileOnly(platform(catalog.findLibrary("androidx-compose-bom").get()))
    compileOnly("androidx.compose.runtime:runtime")
}
