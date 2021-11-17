import java.net.URI

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val kodein_version: String by project
val wirehair_wrapper_version: String by project
val kotlin_logging_version: String by project

plugins {
    application
    kotlin("jvm") version "1.5.31"
}

group = "com.astronaut"
version = "0.0.1"

application {
    mainClass.set("com.astronaut.ApplicationKt")
}

repositories {
    mavenCentral()
    maven { url = URI("https://jitpack.io") }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    implementation("org.kodein.di:kodein-di:$kodein_version")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlin_logging_version")
    implementation("net.java.dev.jna:jna:5.2.0")
    implementation("net.java.dev.jna:jna-platform:5.2.0")
}
