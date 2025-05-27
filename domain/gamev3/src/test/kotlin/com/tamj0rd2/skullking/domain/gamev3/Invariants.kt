package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.assumeWasSuccessful
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.wasSuccessful
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.count
import strikt.assertions.filterIsInstance
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isIn

class Invariants {
    @Test
    fun `a valid game always has 2-6 players`() =
        propertyTest {
            checkAll(Arb.game.validOnly()) { game ->
                expectThat(game.state.players.size).isIn(2..6)
            }
        }

    @Test
    fun `commands never change the players in the game`() =
        propertyTest {
            checkAll(Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.state.players).isEqualTo(initialGame.state.players)
            }
        }

    @Test
    fun `commands never change the game's ID`() =
        propertyTest {
            checkAll(Arb.game.validOnly(), Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.id).isEqualTo(initialGame.id)
            }
        }

    @Test
    fun `a game always start with a GameStarted event`() =
        propertyTest {
            checkAll(Arb.game.validOnly()) { game ->
                expectThat(game.events.first()).isA<GameStartedEvent>()
            }
        }

    @Test
    fun `a game only ever has 1 GameStarted event`() =
        propertyTest {
            checkAll(Arb.game.validOnly()) { game ->
                expectThat(game.events).filterIsInstance<GameStartedEvent>().count().isEqualTo(1)
            }
        }

    @Test
    fun `a game reconstituted from events has the same identity, state and events as the game it was reconstituted from`() =
        propertyTest {
            checkAll(Arb.game.validOnly()) { originalGame ->
                expectThat(Game.reconstitute(originalGame.events)).wasSuccessful().and {
                    isEqualTo(originalGame)
                    get { id }.isEqualTo(originalGame.id)
                    get { state }.isEqualTo(originalGame.state)
                    get { events }.isEqualTo(originalGame.events)
                }
            }
        }
}
