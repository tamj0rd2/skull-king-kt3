package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

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

        // TODO: assert that player1 also received the game update
        player2.received(GameUpdate.GameStarted)
    }

    @Test
    @Disabled
    fun `starting the game starts the round 1 bidding phase`() {
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
    @Disabled
    fun `the game cannot be started with less than 2 players`() {
        TODO()
    }
}
