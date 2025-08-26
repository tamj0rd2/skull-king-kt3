plugins { id("buildlogic.kotlin-application-conventions") }

dependencies {
    implementation(libs.values4k)
    implementation(libs.bundles.http4k.server)
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")

    testImplementation(libs.konsist)
    testImplementation("com.microsoft.playwright:playwright:1.54.0")
    testFixturesImplementation("com.microsoft.playwright:playwright:1.54.0")

    testFixturesImplementation(libs.values4k)
}
