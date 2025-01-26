package com.tamj0rd2.skullking.domain.game

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withName
import com.lemonappdev.konsist.api.verify.assertNotEmpty
import com.tamj0rd2.skullking.domain.game.LobbyActionArbs.validLobbyCommandsArb
import org.junit.jupiter.api.Test
import strikt.api.Assertion.Builder
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isSuccess
import strikt.assertions.single

class LobbyActionsArbsTest {
    private val gameModuleName = "domain/game"

    @Test
    fun `for every LobbyCommand, there is a corresponding lobby command arbitrary`() {
        val lobbyCommandNames =
            Konsist
                .scopeFromProduction(gameModuleName)
                .classes()
                .withName("LobbyCommand")
                .single()
                .classesAndObjects(includeNested = false)
                .apply { assertNotEmpty() }
                .map { it.name }

        val lobbyCommandArbsProperties =
            Konsist
                .scopeFromProject(gameModuleName, "testFixtures")
                .objects()
                .withName("LobbyActionArbs")
                .single()
                .properties(includeNested = false)

        val expectedLobbyCommandArbNames = lobbyCommandNames.map { it.first().lowercase() + it.drop(1) + "LobbyCommandArb" }

        expectThat(lobbyCommandArbsProperties).get { map { it.name } }.contains(expectedLobbyCommandArbNames)

        expectThat(lobbyCommandArbsProperties)
            .get { withName("lobbyCommandArb") }
            .single()
            .get { text }
            .containsAll(expectedLobbyCommandArbNames)

        expectThat(lobbyCommandArbsProperties)
            .get { withName("validLobbyCommandsArb") }
            .single()
            .get { text }
            .containsAll(expectedLobbyCommandArbNames)
    }

    // This ensures the validity of the arb we use for the rest of the tests
    // TODO: could add some statistic here, for example, to show that the actions aren't always empty.
    @Test
    fun `valid lobby commands never throw`() =
        invariant(arb = validLobbyCommandsArb) { lobby, action ->
            expectCatching { lobby.mustExecute(action) }.isSuccess()
        }
}

private fun Builder<String>.containsAll(subStrings: Collection<String>) = apply { subStrings.fold(this, Builder<String>::contains) }
