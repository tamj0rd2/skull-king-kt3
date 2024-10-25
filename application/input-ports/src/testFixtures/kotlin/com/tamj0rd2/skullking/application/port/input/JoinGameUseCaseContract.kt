package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.players
import com.tamj0rd2.skullking.domain.model.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.game.GameIsFull
import com.tamj0rd2.skullking.domain.model.game.PlayerHasAlreadyJoined
import org.junit.jupiter.api.Test
import strikt.api.expectThrows
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo

abstract class JoinGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `can join a game`() {
        val player = scenario.newPlayer()
        val gameId = player.createsAGame()
        val playerId = player.joinsAGame(gameId)
        player.hasGameStateWhere { players.isEqualTo(listOf(playerId)) }
    }

    @Test
    fun `given there is already a player in a game, when another player joins, both players know about the other`() {
        val player1 = scenario.newPlayer()
        val player2 = scenario.newPlayer()

        val gameId = player1.createsAGame()
        player1.joinsAGame(gameId)
        player2.joinsAGame(gameId)

        listOf(player1, player2).each { hasGameStateWhere { players.containsExactlyInAnyOrder(player1.id, player2.id) } }
    }

    @Test
    fun `joining a full game is not possible`() {
        val (gameId, _) = scenario.newGame().withPlayerCount(MAXIMUM_PLAYER_COUNT).done()
        val anotherPlayer = scenario.newPlayer()
        expectThrows<GameIsFull> { anotherPlayer.joinsAGame(gameId) }
    }

    @Test
    fun `the same player cannot join the game more than once`() {
        val player = scenario.newPlayer()
        val gameId = player.createsAGame()
        player.joinsAGame(gameId)
        expectThrows<PlayerHasAlreadyJoined> { player.joinsAGame(gameId) }
    }

    @Test
    fun `joining a game that has started is not possible`() {
        val (gameId, _) =
            scenario
                .newGame()
                .withPlayerCount(MINIMUM_PLAYER_COUNT)
                .start()
                .done()

        val lateJoiningPlayer = scenario.newPlayer()
        expectThrows<GameHasAlreadyStarted> { lateJoiningPlayer.joinsAGame(gameId) }
    }
}
