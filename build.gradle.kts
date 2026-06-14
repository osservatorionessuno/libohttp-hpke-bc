import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
}

group = "org.osservatorionessuno"
version = "1.0.1"

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
    // BouncyCastle is compileOnly: the backend compiles against the BC API but does not impose a
    // provider variant (bcprov-jdk18on vs bcprov-jdk15to18) on consumers — they supply whichever
    // BouncyCastle they already ship, which avoids duplicate-class clashes downstream. Tests bring
    // their own BC below.
    compileOnly(libs.bouncycastle)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bouncycastle)
    testRuntimeOnly(libs.junit.platform.launcher)
}

ktlint {
    version.set("1.3.1")
}

tasks.test {
    useJUnitPlatform()
}

// Reproducible archives
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
