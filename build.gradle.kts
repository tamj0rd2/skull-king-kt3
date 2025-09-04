import com.ncorti.ktfmt.gradle.tasks.KtfmtFormatTask

plugins {
    id("buildlogic.kotlin-application-conventions")
    id("com.ncorti.ktfmt.gradle") version "0.23.0"
}

dependencies {
    implementation(libs.slf4j)
    implementation(libs.values4k)
    implementation(libs.bundles.http4k.server)
    implementation(libs.kotlinx.html)
    implementation(libs.kondor)

    testImplementation(libs.konsist)
    testImplementation(libs.playwright)
    testImplementation(libs.hamkrest)

    testFixturesImplementation(libs.hamkrest)
    testFixturesImplementation(libs.playwright)
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
