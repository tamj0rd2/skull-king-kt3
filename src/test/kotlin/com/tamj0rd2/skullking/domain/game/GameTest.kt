package com.tamj0rd2.skullking.domain.game

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test

class GameTest {
    @Test
    fun `a game can be reconstituted from its events`() {
        val playerId = PlayerId("test-player")
        val game = Game.new(GameId.random(), playerId).execute(GameCommand.AddPlayer(playerId))

        val reconstituted = Game.reconstitute(game.events)

        assertThat(reconstituted, equalTo(game))
    }

    @Test
    fun `when a player joins, an event is recorded`() {
        val playerToJoin = PlayerId("host-player")

        val game = Game.new(GameId.random(), playerToJoin)
        val gameAfterPlayerJoins = game.execute(GameCommand.AddPlayer(playerToJoin))

        val playerJoinedEvents = gameAfterPlayerJoins.events.filterIsInstance<GameEvent.PlayerJoined>()
        assertThat(playerJoinedEvents, equalTo(listOf(GameEvent.PlayerJoined(gameId = game.id, playerId = playerToJoin))))
    }
}
