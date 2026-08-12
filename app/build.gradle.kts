import java.util.Properties
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    id("template.android.application")
    id("template.android.compose")
    id("template.android.hilt")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}
val tmdbAccessToken: String = localProps.getProperty("tmdb.access.token", "")

android {
    namespace = "com.bsp.wsiw"

    defaultConfig {
        applicationId = "com.bsp.wsiw"
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "TMDB_ACCESS_TOKEN", "\"$tmdbAccessToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            buildConfigField("String", "BASE_URL", "\"https://api.themoviedb.org/3/\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "BASE_URL", "\"https://api.themoviedb.org/3/\"")
        }
    }
}

dependencies {
    implementation(project(":core:core-ui"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-network"))
    implementation(project(":layer:layer-data"))
    implementation(project(":core:core-datastore"))
    implementation(project(":feature:feature-home"))
    implementation(project(":feature:feature-detail"))
    implementation(project(":feature:feature-search"))
    implementation(project(":feature:feature-watchlist"))
    implementation(project(":feature:feature-profile"))
    implementation(project(":feature:feature-tv"))

    implementation(catalog.findLibrary("androidx-navigation3-ui").get())

    implementation(platform(catalog.findLibrary("androidx-compose-bom").get()))
    implementation("androidx.compose.material:material-icons-core")

    implementation(catalog.findLibrary("timber").get())
    implementation(catalog.findLibrary("androidx-core-splashscreen").get())
}
