plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(libs.bundles.http4k.server)
    implementation(project(":adapter-esdb"))
    implementation(project(":application"))
    implementation(project(":domain"))
    testImplementation(testFixtures(project(":application")))
    testFixturesImplementation(testFixtures(project(":application")))
    testFixturesImplementation(testFixtures(project(":domain")))
    testFixturesApi(libs.bundles.http4k.client)
}
