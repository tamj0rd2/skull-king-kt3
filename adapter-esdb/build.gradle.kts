plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(libs.values4k)
    implementation(project(":application"))
    testFixturesApi(testFixtures(project(":application")))
    implementation("com.eventstore:db-client-java:5.2.0")
    implementation(libs.bundles.json)
}
