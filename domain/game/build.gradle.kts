plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(libs.values4k)
    api(libs.result4k)
    api(project(":lib:forkhandles-extensions"))
    testImplementation("com.lemonappdev:konsist:0.16.1")
}

tasks {
    test {
        System.setProperty("kotest.proptest.output.shrink-steps", "false")
    }
}
