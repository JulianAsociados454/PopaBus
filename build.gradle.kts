// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.googleServicesPlugin) apply false
    kotlin("jvm")
}
dependencies {
    testImplementation(kotlin("test"))
}
kotlin {
    jvmToolchain(8)
}