package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import dev.forkhandles.values.ZERO
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotEqualTo

abstract class JoinGameGameUseCaseContract {
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
}
