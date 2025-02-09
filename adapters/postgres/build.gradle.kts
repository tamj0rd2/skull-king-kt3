plugins {
    id("buildlogic.kotlin-library-conventions")
    id("org.flywaydb.flyway") version "11.3.1"
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:11.3.1")
    }
}

dependencies {
    forImplementation(libs.bundles.json)
    implementation("org.postgresql:postgresql:42.7.5")
    forImplementation("org.jooq:jooq:3.19.18")
    forImplementation(libs.slf4j)
    forImplementation(project(":domain:game"))
    forImplementation(project(":application:output-ports"), alsoUseForTesting = true)
    forImplementation(project(":lib:common-json"))
}

flyway {
    driver = "org.postgresql.Driver"
    // FIXME: password in plain text
    url = "jdbc:postgresql://localhost:5432/skullking?user=skullking&password=password"
    user = "skullking"
    validateMigrationNaming = true
    defaultSchema = "skullking"
}

tasks.named("processResources") {
    dependsOn("flywayMigrate")
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
