package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import dev.forkhandles.result4k.Failure
import dev.forkhandles.values.random
import net.jqwik.api.Assume
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import strikt.api.expect
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class GameTest {
    @Example
    fun `when a new game is created, a GameCreated event is added to its changes`() {
        val game = Game.new()
        expect {
            that(game.changes).hasSize(1)
            that(game.changes.first()).isA<GameCreated>().get { gameId }.isEqualTo(game.id)
        }
    }

    @Example
    fun `a game can be created from a history of events`() {
        val gameId = GameId.random()
        val player1 = PlayerId.random()
        val player2 = PlayerId.random()
        val game =
            Game.from(
                listOf(
                    GameCreated(gameId),
                    PlayerJoined(gameId, player1),
                    PlayerJoined(gameId, player2),
                ),
            )

        expect {
            that(game.changes).isEmpty()
            that(game.players).isEqualTo(listOf(player1, player2))
        }
    }

    @Example
    fun `a game cannot be built from an empty history`() {
        expectThrows<IllegalStateException> { Game.from(emptyList()) }
    }

    @Example
    fun `joining a full game is not possible`() {
        val game = Game.new()
        repeat(MAXIMUM_PLAYER_COUNT) { game.addPlayer(PlayerId.random()) }

        expect {
            that(game.addPlayer(PlayerId.random())).isA<Failure<GameIsFull>>()
            that(game.players).hasSize(MAXIMUM_PLAYER_COUNT)
        }
    }

    @Property
    fun `a game cannot be built from events that affect multiple different games`(
        @ForAll @IntRange(min = 1, max = MAXIMUM_PLAYER_COUNT) eventCountForThisGame: Int,
        @ForAll @IntRange(min = 1, max = MAXIMUM_PLAYER_COUNT) eventCountForPreviousGames: Int,
    ) {
        Assume.that(eventCountForThisGame + eventCountForPreviousGames <= MAXIMUM_PLAYER_COUNT)

        val thisGameId = GameId.random()

        val eventsForThisGame =
            buildList {
                repeat(eventCountForThisGame) { add(PlayerJoined(thisGameId, PlayerId.random())) }
            }

        val eventsForOtherGames =
            buildList {
                repeat(eventCountForPreviousGames) { add(PlayerJoined(GameId.random(), PlayerId.random())) }
            }

        val eventsContainingMultipleGames = eventsForThisGame.take(1) + (eventsForThisGame.drop(1) + eventsForOtherGames).shuffled()
        expectThrows<IllegalStateException> { Game.from(eventsContainingMultipleGames) }
    }
}
