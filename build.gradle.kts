// Top-level build file — plugin declarations only. Config lives in build-logic convention plugins.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
}

// Aggregate task for CI — runs Detekt across every submodule.
tasks.register("detektAll") {
    group = "verification"
    description = "Runs Detekt on all submodules."
    dependsOn(subprojects.filter { it.buildFile.exists() }.map { "${it.path}:detekt" })
}
