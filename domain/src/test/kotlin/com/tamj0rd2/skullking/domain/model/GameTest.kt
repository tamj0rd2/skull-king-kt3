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

class GameTest {
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
        @ForAll @IntRange(min = 1, max = MAXIMUM_PLAYER_COUNT) eventsForThisGame: Int,
        @ForAll @IntRange(min = 1, max = MAXIMUM_PLAYER_COUNT) eventsForOtherGames: Int,
    ) {
        Assume.that(eventsForThisGame + eventsForOtherGames <= MAXIMUM_PLAYER_COUNT)

        val thisGameId = GameId.random()

        val eventsForThisGame = buildList {
            repeat(eventsForThisGame) { add(PlayerJoined(thisGameId, PlayerId.random())) }
        }

        val eventsForOtherGames = buildList {
            repeat(eventsForOtherGames) { add(PlayerJoined(GameId.random(), PlayerId.random())) }
        }

        val eventsContainingMultipleGames = eventsForThisGame.take(1) + (eventsForThisGame.drop(1) + eventsForOtherGames).shuffled()
        expectThrows<IllegalStateException> { Game(thisGameId, eventsContainingMultipleGames) }
    }
}
