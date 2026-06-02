import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.library")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

android {
    namespace = "com.template.android.core.testing"
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    api(project(":core:core-common"))
    api(catalog.findLibrary("coroutines-test").get())
    api(catalog.findLibrary("junit").get())
    api(catalog.findLibrary("mockk").get())
    api(catalog.findLibrary("turbine").get())
}
