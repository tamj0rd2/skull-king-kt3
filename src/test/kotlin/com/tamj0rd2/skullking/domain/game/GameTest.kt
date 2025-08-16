package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.withSingle

class GameTest {
    @Test
    fun `when a player joins, an event is recorded`() {
        val playerToJoin = PlayerId("host-player")

        val game = Game.new(GameId.random(), playerToJoin)
        val gameAfterPlayerJoins = game.addPlayer(playerToJoin)

        expectThat(gameAfterPlayerJoins) {
            get { players }.containsExactlyInAnyOrder(playerToJoin)
            get { events }
                .withSingle {
                    isA<GameEvent.PlayerJoined>().get { playerId }.isEqualTo(playerToJoin)
                }
        }
    }
}
