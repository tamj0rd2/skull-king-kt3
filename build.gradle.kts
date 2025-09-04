import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask

plugins {
    id("buildlogic.kotlin-application-conventions")
    id("com.ncorti.ktfmt.gradle") version "0.23.0"
}

dependencies {
    implementation(libs.values4k)
    implementation(libs.bundles.http4k.server)
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.8.0")
    implementation(libs.kondor)

    testImplementation(libs.konsist)
    testImplementation("com.microsoft.playwright:playwright:1.54.0")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")

    testFixturesImplementation("com.natpryce:hamkrest:1.8.0.1")
    testFixturesImplementation("com.microsoft.playwright:playwright:1.54.0")
    testFixturesImplementation(libs.values4k)
}

ktfmt {
    maxWidth.set(140)
    kotlinLangStyle()
}

tasks.register<KtfmtFormatTask>("ktfmtPrecommit") {
    source = project.fileTree(rootDir)
    include("**/*.kt", "**/*.kts")
}

listOf("ktfmtCheck", "ktfmtCheckMain", "ktfmtCheckTest", "ktfmtCheckTestFixtures", "ktfmtCheckScripts").forEach {
    tasks.named(it).configure { enabled = false }
}
