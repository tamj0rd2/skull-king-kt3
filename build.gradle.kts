plugins { id("buildlogic.kotlin-application-conventions") }

dependencies {
    implementation(libs.values4k)
    implementation(libs.bundles.http4k.server)

    testImplementation(libs.konsist)
    testImplementation("com.microsoft.playwright:playwright:1.54.0")
    testFixturesImplementation("com.microsoft.playwright:playwright:1.54.0")

    testFixturesImplementation(libs.values4k)
}
