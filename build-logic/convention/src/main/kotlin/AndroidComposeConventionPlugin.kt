import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        target.extensions.findByType(ApplicationExtension::class.java)?.buildFeatures?.compose = true
        target.extensions.findByType(LibraryExtension::class.java)?.buildFeatures?.compose = true
    }
}
