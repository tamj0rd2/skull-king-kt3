package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo

class GameTest {
    @Test
    fun `when a player joins, an event is recorded`() {
        val playerToJoin = PlayerId("host-player")

        val game = Game.new(GameId.random(), playerToJoin)
        val gameAfterPlayerJoins = game.addPlayer(playerToJoin)

        expectThat(gameAfterPlayerJoins) {
            get { players }.containsExactlyInAnyOrder(playerToJoin)
            get { events }
                .isEqualTo(
                    listOf(
                        GameEvent.GameCreated(gameId = game.id, createdBy = playerToJoin),
                        GameEvent.PlayerJoined(gameId = game.id, playerId = playerToJoin),
                    )
                )
        }
    }
}
