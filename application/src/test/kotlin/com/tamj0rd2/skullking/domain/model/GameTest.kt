package com.tamj0rd2.skullking.domain.model

import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import dev.forkhandles.result4k.Failure
import dev.forkhandles.values.random
import net.jqwik.api.Example
import strikt.api.expect
import strikt.assertions.hasSize
import strikt.assertions.isA

class GameTest {
    private val gameEventsPort = GameEventsInMemoryAdapter()

    @Example
    fun `joining a full game is not possible`() {
        with(gameEventsPort) {
            val gameId = GameId.random()
            repeat(Game.MAXIMUM_PLAYER_COUNT) { Game.addPlayer(gameId, PlayerId.random()) }

            expect {
                that(Game.addPlayer(gameId, PlayerId.random())).isA<Failure<GameIsFull>>()
                that(Game.load(gameId).players).hasSize(6)
            }
        }
    }
}
