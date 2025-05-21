package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.assumeSuccess
import com.tamj0rd2.propertytesting.setMaxDiscardPercentage
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteGame
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickStarted
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class InvariantsTest {
    @Test
    fun `a game always has 2-6 players`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                val currentPlayers = game.state.players
                assert(currentPlayers.size >= 2)
                assert(currentPlayers.size <= 6)
            }
        }

    @Test
    fun `the players in the game never change`() =
        propertyTest {
            checkAll(setMaxDiscardPercentage(70), Arb.game, Arb.gameCommand) { initial, command ->
                val updated = initial.execute(command).assumeSuccess()
                assertEquals(initial.state.players, updated.state.players)
            }
        }

    @Test
    fun `the game's id never changes`() =
        propertyTest {
            checkAll(setMaxDiscardPercentage(70), Arb.game, Arb.gameCommand) { initial, command ->
                val updated = initial.execute(command).assumeSuccess()
                assertEquals(initial.id, updated.id)
            }
        }

    @Test
    fun `every event in the game is related to that specific game`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                val gameIdsFromEvents =
                    game.events
                        .map { it.gameId }
                        .toSet()
                assert(gameIdsFromEvents.single() == game.id)
            }
        }

    @Test
    fun `a game that is rebuilt from its history of events has the same identity, events and state as the original`() =
        propertyTest {
            checkAll(Arb.game) { initial ->
                val reconstitutedGame = Game.reconstituteFrom(initial.events).orThrow()
                assertEquals(initial, reconstitutedGame)
                assertEquals(initial.id, reconstitutedGame.id)
                assertEquals(initial.events, reconstitutedGame.events)
                assertEquals(initial.state, reconstitutedGame.state)
            }
        }

    @Test
    fun `each successful command results in 1 event being emitted`() {
        propertyTest {
            checkAll(setMaxDiscardPercentage(70), Arb.game, Arb.gameCommand) { initial, command ->
                val initialEvents = initial.events
                val eventsAfterCommand = initial.execute(command).assumeSuccess().events

                assertEquals(initialEvents.size + 1, eventsAfterCommand.size)
                assertEquals(initialEvents, eventsAfterCommand.dropLast(1))
                assertEquals(
                    expected =
                        when (command) {
                            CompleteGame -> GameCompleted::class
                            is CompleteRound -> RoundCompleted::class
                            is CompleteTrick -> TrickCompleted::class
                            is PlaceABid -> BidPlaced::class
                            is PlayACard -> CardPlayed::class
                            is StartRound -> RoundStarted::class
                            is StartTrick -> TrickStarted::class
                        },
                    actual = eventsAfterCommand.last()::class,
                )
            }
        }
    }

    @Test
    @Disabled("just not implemented yet")
    fun `each successful command results in a state change`() {
        propertyTest {
            checkAll(Arb.game, Arb.gameCommand) { initial, command ->
                val updated = initial.execute(command).assumeSuccess()
                assertNotEquals(initial.state, updated.state)
            }
        }
    }

    @Test
    @Disabled("just not implemented yet")
    fun `a player can never have more than 10 cards`() {
        TODO()
    }
}
