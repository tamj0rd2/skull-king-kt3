package com.tamj0rd2.skullking.domain.game

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

class GameTest {
    @Test
    fun `a game can be reconstituted from its events`() {
        val playerId = PlayerId.of("test-player")
        val game = Game.new(playerId).execute(GameCommand.AddPlayer(playerId))

        val reconstituted = Game.reconstitute(game.events)

        assertThat(reconstituted, equalTo(game))
    }

    @Test
    fun `when a player joins, an event is recorded`() {
        val playerToJoin = PlayerId.of("host-player")

        val game = Game.new(playerToJoin)
        val gameAfterPlayerJoins = game.execute(GameCommand.AddPlayer(playerToJoin))

        val playerJoinedEvents = gameAfterPlayerJoins.events.filterIsInstance<GameEvent.PlayerJoined>()
        assertThat(playerJoinedEvents, equalTo(listOf(GameEvent.PlayerJoined(gameId = game.id, playerId = playerToJoin))))
    }
}
