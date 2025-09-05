package com.tamj0rd2.skullking

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withNameStartingWith
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {
    private val domainPackage = "com.tamj0rd2.skullking.domain.."
    private val applicationPackage = "com.tamj0rd2.skullking.application"
    private val applicationPackageRecursive = "$applicationPackage.."
    private val adaptersPackage = "com.tamj0rd2.skullking.adapters"
    private val adaptersPackageRecursive = "$adaptersPackage.."

    @Test
    fun `layers have correct dependencies to adhere to hexagonal architecture`() {
        Konsist.scopeFromProject().assertArchitecture {
            val domain = Layer("Domain", domainPackage)
            val application = Layer("Application", applicationPackageRecursive)
            val adapters = Layer("Adapters", adaptersPackageRecursive)

            domain.dependsOnNothing()
            application.dependsOn(domain)
            adapters.dependsOn(domain, application)
        }
    }

    val domainLayerAllowList =
        setOf("java.", "com.tamj0rd2.skullking.", "dev.forkhandles.values.", "com.natpryce.hamkrest.", "org.junit", "io.kotest.property.")

    @Test
    fun `the domain layer only uses allowed libraries`() {
        Konsist.scopeFromProject().files.withPackage(domainPackage).assertTrue(strict = true) { file ->
            file.imports.all { import -> domainLayerAllowList.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `the application layer only uses allowed libraries`() {
        val allowList = domainLayerAllowList

        Konsist.scopeFromProject().files.withPackage(domainPackage).assertTrue(strict = true) { file ->
            file.imports.all { import -> allowList.any { import.name.startsWith(it) } }
        }
    }

    @Test
    fun `application ports do not depend on services`() {
        Konsist.scopeFromProject().files.withPackage("$applicationPackage.ports..").assertTrue(strict = true) { file ->
            file.imports.none { import -> import.name.contains("$applicationPackage.services.") }
        }
    }

    @Test
    fun `input ports don't depend on output ports`() {
        Konsist.scopeFromProject().files.withPackage("$applicationPackage.ports.input..").assertTrue(strict = true) { file ->
            file.imports.none { import -> import.name.contains("$applicationPackage.ports.output.") }
        }
    }

    @Test
    fun `output ports don't depend on input ports`() {
        Konsist.scopeFromProject().files.withPackage("$applicationPackage.ports.output..").assertTrue(strict = true) { file ->
            file.imports.none { import -> import.name.contains("$applicationPackage.ports.input.") }
        }
    }

    @Test
    fun `different adapter kinds don't depend on each other`() {
        fun String.adapterSubPackage() = this.substringAfter("$adaptersPackage.").substringBefore(".")

        Konsist.scopeFromProject().files.withPackage(adaptersPackageRecursive).assertTrue(strict = true) { file ->
            val adapterKind = file.packagee!!.name.adapterSubPackage()
            if (adapterKind == "configuration") return@assertTrue true

            file.imports
                .withNameStartingWith("$adaptersPackage.")
                .map { import -> import.name.adapterSubPackage() }
                .all { importedAdapterKind -> importedAdapterKind in setOf(adapterKind, "configuration") }
        }
    }
}
