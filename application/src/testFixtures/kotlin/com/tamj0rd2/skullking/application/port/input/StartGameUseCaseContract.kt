package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.model.game.StartGameErrorCode.TooFewPlayers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThrows

abstract class StartGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `when the game is started, all joined players receive a game update`() {
        val player1 = scenario.newPlayer()
        val player2 = scenario.newPlayer()

        val gameId = player1.createsAGame()
        player1.joinsAGame(gameId)
        player2.joinsAGame(gameId)

        player1.startsTheGame()

        player1.received(GameUpdate.GameStarted)
        player2.received(GameUpdate.GameStarted)
    }

    @Test
    @Disabled
    fun `starting the game begins the round 1 bidding phase`() {
        TODO()
    }

    @Test
    @Disabled
    fun `each player is dealt 1 card`() {
        TODO()
    }

    @Test
    @Disabled
    fun `only the player who created the game can start the game`() {
        TODO()
    }

    @Test
    fun `a game cannot be started with less than 2 players`() {
        val player1 = scenario.newPlayer()
        val gameId = player1.createsAGame()
        player1.joinsAGame(gameId)
        expectThrows<TooFewPlayers> { player1.startsTheGame() }
    }
}
