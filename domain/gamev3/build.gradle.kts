plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(libs.values4k)
    api(libs.result4k)
    api(project(":lib:forkhandles-extensions"))
}

tasks.test {
    systemProperty("junit.jupiter.execution.timeout.test.method.default", "15s")
}
