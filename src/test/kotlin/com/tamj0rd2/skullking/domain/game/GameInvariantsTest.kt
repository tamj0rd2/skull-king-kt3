package com.tamj0rd2.skullking.domain.game

import com.natpryce.hamkrest.allElements
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isA
import com.tamj0rd2.skullking.PropertyTesting.propTestConfig
import com.tamj0rd2.skullking.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameArbs.command
import com.tamj0rd2.skullking.domain.game.GameArbs.game
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test

class GameInvariantsTest {
    @Test
    fun `a game always start with a GameCreated event`() {
        propertyTest { checkAll(propTestConfig, Arb.game) { game -> assertThat(game.events.first(), isA<GameEvent.GameCreated>()) } }
    }

    @Test
    fun `a game only ever has 1 GameCreated event`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game) { game ->
                assertThat(game.events.filterIsInstance<GameEvent.GameCreated>(), hasSize(equalTo(1)))
            }
        }
    }

    @Test
    fun `each event in a game is related to that specific game`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game) { game -> assertThat(game.events, allElements(has(GameEvent::gameId, equalTo(game.id)))) }
        }
    }

    @Test
    fun `each successful command results in 1 new event being emitted`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game, Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command)
                assertThat(updatedGame.events, hasSize(equalTo(initialGame.events.size + 1)))
            }
        }
    }

    @Test
    fun `each successful command results in a state change`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game, Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command)
                assertThat(updatedGame, !equalTo(initialGame))
            }
        }
    }

    @Test
    fun `successful commands never change the game's ID`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game, Arb.command) { initialGame, command ->
                val updatedGame = initialGame.execute(command)
                assertThat(updatedGame.id, equalTo(initialGame.id))
            }
        }
    }

    @Test
    fun `a game reconstituted from events is equal to the game those events came from`() {
        propertyTest {
            checkAll(propTestConfig, Arb.game) { originalGame ->
                val reconstituted = Game.reconstitute(originalGame.events)

                assertThat(reconstituted, equalTo(originalGame))
            }
        }
    }
}
