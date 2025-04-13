import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("buildlogic.kotlin-library-conventions")
    alias(libs.plugins.powerAssert)
}

@OptIn(ExperimentalKotlinGradlePluginApi::class)
powerAssert {
    functions = listOf("kotlin.assert")
    includedSourceSets = listOf("main", "test", "testFixtures")
}

dependencies {
    api(libs.values4k)
    api(libs.result4k)
    api(project(":lib:forkhandles-extensions"))
    testImplementation(kotlin("test"))
    testFixturesImplementation(libs.kotlin.reflection)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-receivers")
    }
}

tasks.test {
    systemProperty("junit.jupiter.execution.parallel.mode.default", "same_thread")
    systemProperty("junit.jupiter.execution.timeout.test.method.default", "20s")
}
