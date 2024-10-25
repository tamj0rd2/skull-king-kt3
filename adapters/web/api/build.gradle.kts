plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(libs.bundles.http4k.api)
    api(project(":domain:auth"))
    api(project(":domain:game"))
    implementation(project(":lib:common-json"))
}
