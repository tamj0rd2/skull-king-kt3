plugins { id("buildlogic.kotlin-application-conventions") }

dependencies {
    forImplementation(libs.bundles.http4k.server)
    forImplementation(libs.slf4j)
    forImplementation(project(":lib:common-json"))
    forImplementation(project(":adapters:esdb"))
    forImplementation(project(":adapters:in-memory"))
    forImplementation(project(":adapters:web:api"))
    forImplementation(project(":application:input-ports"), alsoUseForTesting = true)
    forImplementation(project(":application:output-ports"), alsoUseForTesting = true)
    forImplementation(project(":application:services"), alsoUseForTesting = true)

    forTesting(libs.http4k.core)
    forTesting(project(":adapters:web:client"))
}

tasks {
    test {
        retry {
            maxRetries = 3
            failOnPassedAfterRetry = false

            filter { includeAnnotationClasses.add("*SmokeTest") }
        }
    }
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
