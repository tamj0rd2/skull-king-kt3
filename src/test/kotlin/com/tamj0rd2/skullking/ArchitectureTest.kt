package com.tamj0rd2.skullking

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {
    private val domainPackage = "com.tamj0rd2.skullking.domain.."
    private val applicationPackage = "com.tamj0rd2.skullking.application.."

    @Test
    fun `layers have correct dependencies to adhere to hexagonal architecture`() {
        Konsist.scopeFromProject().assertArchitecture {
            val domain = Layer("Domain", domainPackage)
            val application = Layer("Application", applicationPackage)
            val adapters = Layer("Adapters", "com.tamj0rd2.skullking.adapters..")

            domain.dependsOnNothing()
            application.dependsOn(domain)
            adapters.dependsOn(domain, application)
        }
    }

    @Test
    // write a test that proves that the domain layer does not depend on any external libraries
    fun `domain layer does not depend on external libraries`() {
        Konsist.scopeFromProject().files.withPackage(domainPackage)
    }

    val domainLayerAllowList = listOf<String>()

    @Test
    fun `the domain layer only uses allowed libraries`() {
        Konsist.scopeFromProject().files.withPackage(domainPackage).assertTrue(strict = true) { file
            ->
            file.imports.all { import -> domainLayerAllowList.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `the application layer only uses allowed libraries`() {
        val allowList = domainLayerAllowList

        Konsist.scopeFromProject().files.withPackage(domainPackage).assertTrue(strict = true) { file
            ->
            file.imports.all { import -> allowList.any { import.name.startsWith(it) } }
        }
    }
}
