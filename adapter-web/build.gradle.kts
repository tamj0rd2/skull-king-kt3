plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(libs.bundles.http4k.server)
    implementation(project(":adapter-esdb"))
    implementation(project(":application"))
    testFixturesApi(testFixtures(project(":application")))
    testFixturesApi(libs.bundles.http4k.client)
}
