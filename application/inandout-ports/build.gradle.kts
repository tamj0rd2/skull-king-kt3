plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    forImplementation(project(":domain:auth"), transitive = true, alsoUseForTesting = true)
    forImplementation(project(":domain:game"), transitive = true, alsoUseForTesting = true)
}

private fun DependencyHandlerScope.forImplementation(
    dependency: Any,
    transitive: Boolean = false,
    alsoUseForTesting: Boolean = false,
) {
    if (transitive) api(dependency) else implementation(dependency)
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
