// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("org.jetbrains.kotlin.android") version "2.4.21" apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.googleServicesPlugin) apply false
}

buildscript {
    configurations.classpath {
        resolutionStrategy {
            // Forzamos la versión para que AGP 9 y Gradle se pongan de acuerdo
            force("org.jetbrains:annotations:23.0.0")
        }
    }
}