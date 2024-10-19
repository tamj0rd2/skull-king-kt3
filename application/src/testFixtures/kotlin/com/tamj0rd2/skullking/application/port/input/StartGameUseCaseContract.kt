package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole.PlayerGameState.Companion.hand
import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole.PlayerGameState.Companion.roundNumber
import com.tamj0rd2.skullking.domain.model.game.RoundNumber
import com.tamj0rd2.skullking.domain.model.game.StartGameErrorCode.TooFewPlayers
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

abstract class StartGameUseCaseContract {
    protected abstract val scenario: TestScenario

    // TODO: after this, write a test for a BiddingUseCase. When the game is started, it should be possible to bid 0 or 1.
    @Test
    fun `starting the game begins round 1`() {
        val (_, players) = scenario.newGame().withMinimumPlayersToStart().done()
        players.first().startsTheGame()
        players.each { gameState { roundNumber.isEqualTo(RoundNumber.of(1)) } }
    }

    @Test
    fun `each player is dealt 1 card`() {
        val (_, players) = scenario.newGame().withMinimumPlayersToStart().done()
        players.first().startsTheGame()
        players.each { gameState { hand.hasSize(1) } }
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
