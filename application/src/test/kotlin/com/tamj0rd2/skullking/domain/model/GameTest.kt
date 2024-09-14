package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import dev.forkhandles.result4k.Failure
import dev.forkhandles.values.random
import net.jqwik.api.Example
import strikt.api.expect
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
}
