package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.domain.GameArbs.gameArb
import com.tamj0rd2.skullking.domain.GameArbs.gameEventsArb
import com.tamj0rd2.skullking.domain.GameArbs.playerIdArb
import com.tamj0rd2.skullking.domain.GameArbs.validGameEventsArb
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.propertyTest
import com.tamj0rd2.skullking.domain.wasSuccessful
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.next
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.containsExactly
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isSuccess

class GameTest {
    @Test
    fun `when a new game is created, a GameCreated event is added to its changes`() {
        val game = Game.new()
        expectThat(game.events) {
            hasSize(1)
            first().isA<GameCreated>().get { gameId }.isEqualTo(game.id)
        }
    }

    @Test
    fun `joining a full game is not possible`() =
        propertyTest {
            checkAll(
                gameArb.filter { it.state.players.size == MAXIMUM_PLAYER_COUNT },
                playerIdArb,
            ) { game, playerWhoWantsToJoin ->
                val initialPlayers = game.state.players
                val initialEvents = game.events

                assume(initialPlayers.size == MAXIMUM_PLAYER_COUNT)
                assume(!initialPlayers.contains(playerWhoWantsToJoin))

                expectThat(game.addPlayer(playerWhoWantsToJoin)).isA<Failure<GameIsFull>>()
                expectThat(game).run {
                    get { state.players }.isEqualTo(initialPlayers)
                    get { events }.isEqualTo(initialEvents)
                }
            }
        }

    @Test
    fun `a player cannot join the same game twice`() =
        propertyTest {
            checkAll(
                gameArb.filter { it.state.players.size < MAXIMUM_PLAYER_COUNT - 2 },
                playerIdArb,
            ) { game, playerWhoWantsToJoin ->
                assume(game.state.players.size <= MAXIMUM_PLAYER_COUNT - 2)
                assume(!game.state.players.contains(playerWhoWantsToJoin))

                expectThat(game.addPlayer(playerWhoWantsToJoin)).describedAs("joining the first time").wasSuccessful()
                val playersBeforeSecondJoin = game.state.players
                val eventsBeforeSecondJoin = game.events

                expectThat(game.addPlayer(playerWhoWantsToJoin)).describedAs("trying to join again").isA<Failure<PlayerHasAlreadyJoined>>()
                expectThat(game) {
                    get { state.players }.isEqualTo(playersBeforeSecondJoin)
                    get { events }.isEqualTo(eventsBeforeSecondJoin)
                }
            }
        }

    @Nested
    inner class StartingAGame {
        @Test
        fun `when the game is started, each player is dealt 1 card`() {
            val game = gameArb.filter { it.state.players.size >= MINIMUM_PLAYER_COUNT }.next()
            val players = game.state.players

            game.start().orThrow()

            expectThat(game.state.playerHands.keys).containsExactly(players)
            expectThat(game.state.playerHands.values).all { hasSize(1) }
        }

        @Test
        fun `the game cannot be started with less than 2 players`() {
            // TODO: make this a property test

            val game = Game.new()
            game.addPlayer(PlayerId.random()).orThrow()
            expectThrows<StartGameErrorCode> { game.start().orThrow() }
        }

        // TODO: each player has 1 card
        // TODO: starting the game starts the round 1 bidding phase
        // TODO: only the player who created the game can start the game
        // TODO: the game cannot be started when there are less than 2 players
    }

    @Nested
    inner class RestoringAGameFromEvents {
        @Test
        fun `a game can be created from a history of events`() =
            propertyTest {
                checkAll(validGameEventsArb) { events ->
                    assume(events.isNotEmpty())

                    expectCatching { Game.from(events) }.isSuccess().and {
                        get { this.events }.hasSize(events.size).isEqualTo(events)
                    }
                }
            }

        @Test
        fun `a game cannot be built from an empty history`() {
            expectThrows<IllegalStateException> { Game.from(emptyList()) }
        }

        @Test
        fun `a game's history can only ever have 1 game created event`() =
            propertyTest {
                checkAll(gameEventsArb) { events ->
                    assume(events.count { it is GameCreated } > 1)
                    expectCatching { Game.from(events) }.isFailure()
                }
            }

        @Test
        fun `a game cannot be built from events that affect multiple different games`() =
            propertyTest {
                checkAll(
                    validGameEventsArb,
                    validGameEventsArb,
                ) { eventsForThisGame, eventsForADifferentGame ->
                    assume(eventsForThisGame.isNotEmpty())
                    assume(eventsForADifferentGame.isNotEmpty())
                    assume(eventsForADifferentGame.any { it.gameId != eventsForThisGame.first().gameId })

                    val eventsContainingMultipleGames =
                        eventsForThisGame.take(1) + (eventsForThisGame.drop(1) + eventsForADifferentGame).shuffled()
                    expectThrows<IllegalStateException> { Game.from(eventsContainingMultipleGames) }
                }
            }
    }

    @Test
    @Disabled
    fun `joining a game that has started is not possible`() {
        TODO()
    }
}
