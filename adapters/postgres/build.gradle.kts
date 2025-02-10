import org.jooq.meta.jaxb.Logging.WARN

plugins {
    id("buildlogic.kotlin-library-conventions")
    id("org.flywaydb.flyway") version "11.3.1"
    id("org.jooq.jooq-codegen-gradle") version "3.19.18"
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-database-postgresql:11.3.1")
        classpath("org.jooq:jooq:3.19.18")
        classpath("org.jooq:jooq-meta:3.19.18")
        classpath("org.jooq:jooq-codegen:3.19.18")
        classpath("org.postgresql:postgresql:42.7.5")
    }
}

dependencies {
    forImplementation(libs.bundles.json)
    forImplementation("org.postgresql:postgresql:42.7.5")
    jooqCodegen("org.postgresql:postgresql:42.7.5")
    forImplementation("org.jooq:jooq:3.19.18")
    forImplementation("org.jooq:jooq-meta:3.19.18")
    forImplementation("org.jooq:jooq-codegen:3.19.18")
    forImplementation(libs.slf4j)
    forImplementation(project(":domain:game"))
    forImplementation(project(":application:output-ports"), alsoUseForTesting = true)
    forImplementation(project(":lib:common-json"))
}

// FIXME: password in plain text
val connectionString = "jdbc:postgresql://localhost:5432/skullking?user=skullking&password=password"
val defaultDbSchema = "skullking"

flyway {
    driver = "org.postgresql.Driver"
    url = connectionString
    validateMigrationNaming = true
    defaultSchema = defaultDbSchema
}

// https://www.jooq.org/doc/latest/manual/code-generation/codegen-configuration/
jooq {
    configuration {
        logging = WARN

        jdbc {
            driver = "org.postgresql.Driver"
            url = connectionString
        }

        generator {
            name = "org.jooq.codegen.KotlinGenerator"

            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = defaultDbSchema

                target {
                    packageName = "com.tamj0rd2.skullking.adapter.postgres"
                }
            }
        }
    }
}

// make sure flyway migrate happens before jooq codegen
tasks.named("jooqCodegen") {
    dependsOn("flywayMigrate")
}

tasks.compileKotlin {
    dependsOn("jooqCodegen")
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
