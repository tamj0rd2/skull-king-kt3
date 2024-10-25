plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(libs.bundles.http4k.server)
    implementation(project(":adapters:esdb"))
    implementation(project(":application"))
    implementation(project(":domain:game"))
    testImplementation(testFixtures(project(":application")))
    testFixturesImplementation(testFixtures(project(":application")))
    testFixturesImplementation(testFixtures(project(":domain:game")))
    testFixturesApi(libs.bundles.http4k.client)
    testFixturesApi(libs.http4k.strikt)
}
