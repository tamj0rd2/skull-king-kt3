plugins { id("buildlogic.kotlin-library-conventions") }

dependencies {
    forImplementation(project(":domain:game"), alsoUseForTesting = true)
    forImplementation(project(":application:input-ports"), alsoUseForTesting = true)
    forImplementation(project(":application:output-ports"), alsoUseForTesting = true)

    forTesting(libs.konsist)
    forTesting(project(":adapters:in-memory"))
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
