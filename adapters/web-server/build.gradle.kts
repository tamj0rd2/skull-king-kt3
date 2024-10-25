plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    forImplementation(libs.bundles.http4k.server)
    forImplementation(project(":adapters:esdb"))
    forImplementation(project(":adapters:in-memory"))
    forImplementation(project(":adapters:web-api"))
    forImplementation(project(":application:input-ports"), alsoUseForTesting = true)
    forImplementation(project(":application:output-ports"), alsoUseForTesting = true)
    forImplementation(project(":application:services"), alsoUseForTesting = true)

    forTesting(libs.http4k.core)
    forTesting(project(":adapters:web-client"))
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
