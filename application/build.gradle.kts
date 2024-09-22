plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(libs.values4k)
    implementation(project(":domain"))
    testFixturesImplementation(project(":domain"))
    testFixturesImplementation(testFixtures(project(":domain")))
}
