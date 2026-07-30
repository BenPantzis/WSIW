pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Workaround for https://github.com/gradle/gradle/issues/28407 â€”
// Android Studio's "Rebuild" action injects testClasses for included builds, which
// deadlocks against clean in Gradle 9.x's composite build scheduler.
gradle.startParameter.excludedTaskNames.addAll(listOf(":build-logic:convention:testClasses"))

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WSIW"

include(":app")
include(":core:core-common")
include(":layer:layer-domain")
include(":layer:layer-data")
include(":core:core-network")
include(":core:core-database")
include(":core:core-ui")
include(":core:core-datastore")
include(":core:core-testing")
include(":feature:feature-home")
include(":feature:feature-detail")
include(":feature:feature-search")
include(":feature:feature-watchlist")
include(":feature:feature-profile")
