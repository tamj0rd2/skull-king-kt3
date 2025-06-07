package com.tamj0rd2.skullking

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.architecture.KoArchitectureCreator.assertArchitecture
import com.lemonappdev.konsist.api.architecture.Layer
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withParentInterface
import com.lemonappdev.konsist.api.ext.list.withoutNameEndingWith
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {
    private val domainLayer = Layer("Domain", "com.tamj0rd2.skullking.domain..")
    private val applicationLayer = Layer("Application", "com.tamj0rd2.skullking.application..")
    private val adapterLayer = Layer("Adapter", "com.tamj0rd2.skullking.adapter..")

    @Test
    fun `the domain doesn't depend on any other packages`() {
        Konsist.scopeFromProject().assertArchitecture { domainLayer.dependsOnNothing() }
    }

    @Test
    fun `the application depends on the domain`() {
        Konsist.scopeFromProduction().assertArchitecture { applicationLayer.dependsOn(domainLayer) }
    }

    @Test
    fun `the application does not depend on adapters`() {
        Konsist.scopeFromProduction().assertArchitecture {
            applicationLayer.doesNotDependOn(adapterLayer)
        }
    }

    @Test
    fun `use cases live in the application layer`() {
        val scope =
            Konsist.scopeFromProduction()
                .classesAndInterfacesAndObjects()
                .withNameEndingWith("UseCase")
        scope.assertNotEmpty()
        scope.assertTrue { it.resideInPackage("com.tamj0rd2.skullking.application..") }
    }

    @Test
    fun `use case implementations live in the application layer`() {
        val scope =
            Konsist.scopeFromProduction()
                .classesAndInterfacesAndObjects()
                .withoutNameEndingWith("Application", "UseCases")
                .withParentInterface { it.hasNameEndingWith("UseCase") }

        scope.assertNotEmpty()
        scope.assertTrue { it.resideInPackage("com.tamj0rd2.skullking.application..") }
        scope.assertTrue { it.name.endsWith("Service") }
    }
}
