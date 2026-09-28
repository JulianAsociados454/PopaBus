plugins {
    kotlin("jvm") version "2.4.20" // o la versión que te haya puesto
    id("io.ktor.plugin") version "3.6.0" // o la versión que te haya puesto
    kotlin("plugin.serialization") version "2.4.20" // <-- AGREGA ESTA LÍNEA
}

group = "com.example"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "com.example.MainKt"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-call-logging-jvm")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
