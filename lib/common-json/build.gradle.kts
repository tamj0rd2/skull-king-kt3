plugins { id("buildlogic.kotlin-library-conventions") }

dependencies {
    implementation(libs.bundles.json)
    implementation(project(":domain:game"))
}
