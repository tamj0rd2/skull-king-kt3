package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.single

class GameTest {
    @Test
    fun `a game can be reconstituted from its events`() {
        val playerId = PlayerId("test-player")
        val game = Game.new(GameId.random(), playerId).addPlayer(playerId)

        val reconstituted = Game.reconstitute(game.events)

        expectThat(reconstituted) {
            get { id }.isEqualTo(game.id)
            get { events }.isEqualTo(game.events)
        }
    }

    @Test
    fun `new events reflects the events that have occurred since the last time the game was saved`() {
        val playerId = PlayerId("test-player")
        val game = Game.new(GameId.random(), playerId)
        expectThat(game.newEvents).single().isA<GameEvent.GameCreated>()

        val gameWithPlayerAdded = Game.reconstitute(game.events).addPlayer(playerId)
        expectThat(gameWithPlayerAdded.newEvents).single().isA<GameEvent.PlayerJoined>()
    }

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
