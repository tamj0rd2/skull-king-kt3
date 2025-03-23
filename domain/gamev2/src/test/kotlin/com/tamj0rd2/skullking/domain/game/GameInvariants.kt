package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.filterIsInstance
import strikt.assertions.first
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThanOrEqualTo
import strikt.assertions.isLessThanOrEqualTo
import strikt.assertions.one
import strikt.assertions.size

class GameInvariants {
    @Test
    fun `a game always starts with a GameStarted event`() =
        gameInvariant { game ->
            expectThat(game.state.events).first().isA<GameStarted>()
        }

    @Test
    fun `the GameStarted event only ever appears once`() =
        gameInvariant { game ->
            expectThat(game.state.events).one { isA<GameStarted>() }
        }

    @Test
    fun `a game always has 2-6 players`() =
        gameInvariant { game ->
            expectThat(game.state.players) {
                size.isGreaterThanOrEqualTo(2)
                size.isLessThanOrEqualTo(6)
            }
        }

    @Test
    fun `the players in the game never change`() =
        propertyTest {
            checkAll(validPlayerIdsArb, validGameCommandsArb) { initialPlayerIds, gameCommands ->
                val game = Game(initialPlayerIds)

                gameCommands.forEach {
                    game.execute(it)
                    expectThat(game.state.players).isEqualTo(initialPlayerIds)
                }
            }
        }

    @Test
    fun `a maximum of 10 rounds can be played`() =
        gameInvariant { game ->
            expectThat(game.state.events).filterIsInstance<RoundStarted>().size.isLessThanOrEqualTo(10)
            expectThat(game.state.events).filterIsInstance<RoundCompleted>().size.isLessThanOrEqualTo(10)
        }

    @Test
    @Disabled
    fun `a player can never have more than 10 cards`() = gameInvariant()
}
