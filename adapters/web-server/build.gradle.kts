plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(libs.bundles.http4k.server)
    implementation(project(":adapters:esdb"))
    implementation(project(":adapters:web-api"))
    implementation(project(":application"))
    implementation(project(":domain:game"))

    testImplementation(testFixtures(project(":application")))
    testImplementation(project(":adapters:web-client"))

    testFixturesImplementation(libs.http4k.core)
    testFixturesImplementation(testFixtures(project(":application")))
    testFixturesImplementation(project(":adapters:web-client"))
    testFixturesImplementation(testFixtures(project(":domain:game")))
}
