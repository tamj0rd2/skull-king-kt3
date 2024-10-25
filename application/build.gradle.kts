plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    forImplementation(project(":domain:auth"), alsoUseForTesting = true)
    forImplementation(project(":domain:game"), alsoUseForTesting = true)
    forImplementation(project(":application:output-ports"), alsoUseForTesting = true)
    forImplementation(project(":adapters:in-memory"), alsoUseForTesting = true)

    forTesting(libs.konsist)
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
