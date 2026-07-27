import com.android.build.api.dsl.LibraryExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("io.gitlab.arturbosch.detekt")
            extensions.configure<LibraryExtension> {
                compileSdk = libs.findVersion("compileSdk").get().toString().toInt()
                defaultConfig {
                    minSdk = libs.findVersion("minSdk").get().toString().toInt()
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }
            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                config.setFrom(rootProject.files("detekt.yml"))
            }
            dependencies {
                "detektPlugins"(libs.findLibrary("detekt-formatting").get())
            }
        }
    }
}
