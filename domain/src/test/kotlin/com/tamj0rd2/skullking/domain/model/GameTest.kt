package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.domain.GameArbs.gameIdArb
import com.tamj0rd2.skullking.domain.PlayerArbs.playerIdsArb
import com.tamj0rd2.skullking.domain.SkullKingArbs
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import net.jqwik.api.Assume
import net.jqwik.api.Disabled
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide
import net.jqwik.kotlin.api.combine
import strikt.api.Assertion
import strikt.api.expect
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isSuccess

@SkullKingArbs
class GameTest {
    @Example
    fun `when a new game is created, a GameCreated event is added to its changes`() {
        val game = Game.new()
        expect {
            that(game.events).hasSize(1)
            that(game.events.first()).isA<GameCreated>().get { gameId }.isEqualTo(game.id)
        }
    }

    @Property
    fun `a game can be created from a history of events`(
        @ForAll events: List<GameEvent>,
    ) {
        Assume.that(events.isNotEmpty())

        expectCatching { Game.from(events) }.isSuccess().and {
            get { this.events }.hasSize(events.size).isEqualTo(events)
        }
    }

    @Example
    fun `a game cannot be built from an empty history`() {
        expectThrows<IllegalStateException> { Game.from(emptyList()) }
    }

    @Property
    fun `a game cannot be built from events that affect multiple different games`(
        @ForAll eventsForThisGame: List<GameEvent>,
        @ForAll eventsForADifferentGame: List<GameEvent>,
    ) {
        Assume.that(eventsForThisGame.isNotEmpty())
        Assume.that(eventsForADifferentGame.isNotEmpty())
        Assume.that(eventsForADifferentGame.any { it.gameId != eventsForThisGame.first().gameId })

        val eventsContainingMultipleGames = eventsForThisGame.take(1) + (eventsForThisGame.drop(1) + eventsForADifferentGame).shuffled()
        expectThrows<IllegalStateException> { Game.from(eventsContainingMultipleGames) }
    }

    @Property
    fun `joining a full game is not possible`(
        @ForAll("aFullGame") events: List<GameEvent>,
        @ForAll playerWhoWantsToJoin: PlayerId,
    ) {
        val game = Game.from(events)
        val initialPlayers = game.state.players

        Assume.that(initialPlayers.size == MAXIMUM_PLAYER_COUNT)
        Assume.that(!initialPlayers.contains(playerWhoWantsToJoin))

        expectThat(game.addPlayer(playerWhoWantsToJoin)).isA<Failure<GameIsFull>>()
        expectThat(game).run {
            get { state.players }.isEqualTo(initialPlayers)
            get { this.events }.isEqualTo(events)
        }
    }

    @Property
    fun `a player cannot join the same game twice`(
        @ForAll("aGameWithSpaceToJoin") events: List<GameEvent>,
        @ForAll playerWhoWantsToJoin: PlayerId,
    ) {
        val game = Game.from(events)
        Assume.that(game.state.players.size <= MAXIMUM_PLAYER_COUNT - 2)
        Assume.that(!game.state.players.contains(playerWhoWantsToJoin))

        expectThat(game.addPlayer(playerWhoWantsToJoin)).describedAs("joining the first time").wasSuccessful()
        val playersBeforeSecondJoin = game.state.players
        val updatesBeforeSecondJoin = game.events

        expectThat(game.addPlayer(playerWhoWantsToJoin)).describedAs("trying to join again").isA<Failure<PlayerHasAlreadyJoined>>()
        expectThat(game) {
            get { state.players }.isEqualTo(playersBeforeSecondJoin)
            get { this.events }.isEqualTo(updatesBeforeSecondJoin)
        }
    }

    @Disabled
    @Property
    fun `joining a game that has started is not possible`() {
        TODO()
    }

    @Provide
    fun aFullGame() =
        combine {
            val gameId by gameIdArb()
            val playerIds by playerIdsArb().ofSize(MAXIMUM_PLAYER_COUNT)

            combineAs {
                listOf(GameCreated(gameId = gameId)) + playerIds.map { playerId -> PlayerJoined(gameId = gameId, playerId = playerId) }
            }
        }

    @Provide
    fun aGameWithSpaceToJoin() =
        combine {
            val gameId by gameIdArb()
            val playerIds by playerIdsArb().ofMaxSize(MAXIMUM_PLAYER_COUNT - 1)

            combineAs {
                listOf(GameCreated(gameId = gameId)) + playerIds.map { playerId -> PlayerJoined(gameId = gameId, playerId = playerId) }
            }
        }
}

private fun <T, E> Assertion.Builder<Result4k<T, E>>.wasSuccessful() = run { isA<Success<*>>() }
