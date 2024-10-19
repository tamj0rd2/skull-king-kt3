plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(libs.values4k)
    implementation(project(":domain:game"))
    testFixturesImplementation(project(":domain:auth"))
    testFixturesImplementation(project(":domain:game"))
    testFixturesImplementation(testFixtures(project(":domain:game")))
    testImplementation("com.lemonappdev:konsist:0.16.1")
}
