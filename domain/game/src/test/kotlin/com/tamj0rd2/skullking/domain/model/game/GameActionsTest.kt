package com.tamj0rd2.skullking.domain.model.game

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.declaration.KoFunctionDeclaration
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withPublicOrDefaultModifier
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.ext.list.withNameStartingWith
import com.lemonappdev.konsist.api.ext.provider.declarationsOf
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.tamj0rd2.skullking.domain.GameActionArbs.validGameActionsArb
import org.junit.jupiter.api.Test
import strikt.api.Assertion.Builder
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isSuccess
import strikt.assertions.single

class GameActionsTest {
    private val gameModuleName = "domain/game"

    @Test
    fun `for every action in the Game, there is a corresponding game action arbitrary`() {
        val publicGameMethodNames =
            Konsist
                .scopeFromProduction(gameModuleName)
                .classes()
                .withName("Game")
                .single()
                .declarationsOf<KoFunctionDeclaration>(includeNested = false)
                .withPublicOrDefaultModifier()
                .apply { assertNotEmpty() }
                .map { it.name }

        val gameActionArbsProperties =
            Konsist
                .scopeFromProject(gameModuleName, "testFixtures")
                .objects()
                .withName("GameActionArbs")
                .single()
                .properties(includeNested = false)

        val expectedGameActionArbNames = publicGameMethodNames.map { "${it}GameActionArb" }

        expectThat(gameActionArbsProperties)
            .get { withNameStartingWith(publicGameMethodNames).map { it.name } }
            .containsExactlyInAnyOrder(expectedGameActionArbNames)

        expectThat(gameActionArbsProperties)
            .get { withName("gameActionArb") }
            .single()
            .get { text }
            .containsAll(expectedGameActionArbNames)

        expectThat(gameActionArbsProperties)
            .get { withName("validGameActionsArb") }
            .single()
            .get { text }
            .containsAll(expectedGameActionArbNames)
    }

    // This ensures the validity of the arb we use for the rest of the tests
    // TODO: could add some statistic here, for example, to show that the actions aren't always empty.
    @Test
    fun `valid game actions never throw`() =
        invariant(arb = validGameActionsArb) { game, action ->
            expectCatching { action.applyTo(game) }.isSuccess()
        }
}

private fun Builder<String>.containsAll(subStrings: Collection<String>) = apply { subStrings.fold(this, Builder<String>::contains) }
