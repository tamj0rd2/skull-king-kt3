plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    implementation(libs.result4k)

    testFixturesImplementation(libs.result4k)
}
