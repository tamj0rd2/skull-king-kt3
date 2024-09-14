package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import dev.forkhandles.result4k.Failure
import dev.forkhandles.values.random
import net.jqwik.api.Example
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

    @Example
    fun `creating a game from events in an invalid order fails`() {
        val events =
            buildList {
                repeat(MAXIMUM_PLAYER_COUNT + 1) {
                    add(PlayerJoined(GameId.random(), PlayerId.random()))
                }
            }

        expectThrows<GameIsFull> { Game(GameId.random(), events) }
    }
}
