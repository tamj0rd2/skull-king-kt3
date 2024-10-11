package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameIsFull
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import dev.forkhandles.values.ZERO
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isNotEqualTo

abstract class JoinGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `can join a game`() {
        val player = scenario.newPlayer()
        val gameId = player.createsAGame()
        val playerId = player.joinsAGame(gameId)
        expectThat(playerId).isNotEqualTo(PlayerId.ZERO)
    }

    @Test
    fun `given there is already a player in a game, when another player joins, the first player is notified`() {
        val player1 = scenario.newPlayer()
        val player2 = scenario.newPlayer()

        val gameId = player1.createsAGame()
        player1.joinsAGame(gameId)
        player2.joinsAGame(gameId)

        player1.received(GameUpdate.PlayerJoined(player2.id))
    }

    @Test
    @Disabled
    fun `joining a full game is not possible`() {
        val players = buildList { repeat(Game.MAXIMUM_PLAYER_COUNT) { add(scenario.newPlayer()) } }

        val gameId = players.first().createsAGame()
        players.forEach { it.joinsAGame(gameId) }

        // TODO: right now, the server is throwing whenever there are failures. That needs fixing.
        val anotherPlayer = scenario.newPlayer()
        expectThrows<GameIsFull> { anotherPlayer.joinsAGame(gameId) }
    }

    @Test
    @Disabled
    fun `joining a game that has started is not possible`() {
        TODO()
    }
}
