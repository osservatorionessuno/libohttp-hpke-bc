import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "org.osservatorionessuno"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

// Java 8 bytecode: runs on JDK 8+ and Android (D8) down to old API levels.
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}

dependencies {
    api(libs.libohttp)
    implementation(libs.bouncycastle)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bouncycastle)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}

// Reproducible archives
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
