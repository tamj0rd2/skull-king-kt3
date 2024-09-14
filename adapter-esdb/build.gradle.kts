plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    api(libs.values4k)
    api("com.eventstore:db-client-java:5.2.0")
    implementation(project(":application"))
    testFixturesApi(testFixtures(project(":application")))
    implementation(libs.bundles.json)
}
