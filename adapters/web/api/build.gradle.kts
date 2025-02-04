plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    forImplementation(libs.bundles.http4k.api)
    forImplementation(project(":domain:game"), transitive = true)
    forImplementation(project(":lib:common-json"))
    forImplementation(project(":application:inandout-ports"))
}

// TODO: is there a way I can share this code rather than copy-pasting everywhere?
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
