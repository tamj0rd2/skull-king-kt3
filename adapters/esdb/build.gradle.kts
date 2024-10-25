plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    forImplementation(libs.bundles.json)
    forImplementation(libs.eventstoredb)
    forImplementation(project(":domain:game"))
    forImplementation(project(":application"), alsoUseForTesting = true)
}

private fun DependencyHandlerScope.forImplementation(
    dependency: Any,
    alsoUseForTesting: Boolean = false,
) {
    implementation(dependency)
    if (alsoUseForTesting) forTesting(dependency)
}

private fun DependencyHandlerScope.forTesting(dependency: Any) {
    testImplementation(dependency)
    testFixturesImplementation(dependency)

    if (dependency is ProjectDependency) {
        testImplementation(testFixtures(dependency))
        testFixturesImplementation(testFixtures(dependency))
    }
}
