plugins {
    kotlin("jvm") version "1.3.50"
}

group = "org.brianbrown"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    implementation("io.github.microutils:kotlin-logging:1.7.6")
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.0")
    testImplementation("org.junit.jupiter", "junit-jupiter", "5.5.2")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("reflect"))
}
