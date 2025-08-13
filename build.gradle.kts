plugins { id("buildlogic.kotlin-application-conventions") }

dependencies {
    implementation(libs.values4k)

    testImplementation(libs.konsist)
}
