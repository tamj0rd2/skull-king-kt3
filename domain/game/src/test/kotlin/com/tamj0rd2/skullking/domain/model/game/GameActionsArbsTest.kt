package com.tamj0rd2.skullking.domain.model.game

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.tamj0rd2.skullking.domain.GameActionArbs.validGameActionsArb
import com.tamj0rd2.skullking.domain.mustExecute
import org.junit.jupiter.api.Test
import strikt.api.Assertion.Builder
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isSuccess
import strikt.assertions.single

class GameActionsArbsTest {
    private val gameModuleName = "domain/game"

    @Test
    fun `for every action in the Game, there is a corresponding game action arbitrary`() {
        val gameCommandNames =
            Konsist
                .scopeFromProduction(gameModuleName)
                .classes()
                .withName("GameAction")
                .single()
                .classesAndObjects(includeNested = false)
                .apply { assertNotEmpty() }
                .map { it.name }

        val gameActionArbsProperties =
            Konsist
                .scopeFromProject(gameModuleName, "testFixtures")
                .objects()
                .withName("GameActionArbs")
                .single()
                .properties(includeNested = false)

        val expectedGameActionArbNames = gameCommandNames.map { it.first().lowercase() + it.drop(1) + "GameActionArb" }

        expectThat(gameActionArbsProperties).get { map { it.name } }.contains(expectedGameActionArbNames)

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
            expectCatching { game.mustExecute(action) }.isSuccess()
        }
}

private fun Builder<String>.containsAll(subStrings: Collection<String>) = apply { subStrings.fold(this, Builder<String>::contains) }
