plugins { id("buildlogic.kotlin-library-conventions") }

dependencies {
  implementation(libs.bundles.http4k.client)
  implementation(project(":adapters:web:api"))
  implementation(project(":application:input-ports"))
  implementation(project(":application:output-ports"))
}
