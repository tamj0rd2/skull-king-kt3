package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.hand
import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.roundNumber
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.RoundNumber
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThrows
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

abstract class StartGameUseCaseContract {
    protected abstract val scenario: TestScenario

    private val gameSetupArb =
        Arb
            .int(min = Game.MINIMUM_PLAYER_COUNT, max = Game.MAXIMUM_PLAYER_COUNT)
            .map { scenario.newGame(playerCount = it).second }

    // TODO: after this, write a test for a BiddingUseCase. When the game is started, it should be possible to bid 0 or 1.
    @Test
    fun `starting the game begins round 1`() =
        propertyTest {
            checkAll(gameSetupArb) { players ->
                players.first().startsTheGame()
                players.each { hasGameStateWhere { roundNumber.isEqualTo(RoundNumber.of(1)) } }
            }
        }

    @Test
    fun `each player is dealt 1 card`() =
        propertyTest {
            checkAll(gameSetupArb) { players ->
                players.first().startsTheGame()
                players.each { hasGameStateWhere { hand.hasSize(1) } }
            }
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
