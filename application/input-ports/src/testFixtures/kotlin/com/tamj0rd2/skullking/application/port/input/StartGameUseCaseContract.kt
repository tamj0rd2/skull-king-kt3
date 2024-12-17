package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.hand
import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.roundNumber
import com.tamj0rd2.skullking.domain.game.GameArbs.validPlayerCountToStartAGame
import com.tamj0rd2.skullking.domain.game.RoundNumber
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

abstract class StartGameUseCaseContract {
    protected abstract val scenario: TestScenario

    // TODO: after this, write a test for a BiddingUseCase. When the game is started, it should be possible to bid 0 or 1.
    @Test
    fun `starting the game begins round 1`() {
        propertyTest {
            checkAll(Arb.validPlayerCountToStartAGame) { playerCount ->
                val gameCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(playerCount - 1)

                Given {
                    gameCreator.`has created a game`()
                    gameCreator.invites(otherPlayers)
                    otherPlayers.each { `accept the game invite`() }
                }

                When {
                    gameCreator.`starts the game`()
                }

                Then {
                    (gameCreator + otherPlayers).each { hasGameStateWhere { roundNumber.isEqualTo(RoundNumber.of(1)) } }
                }
            }
        }
    }

    @Test
    fun `each player is dealt 1 card`() =
        propertyTest {
            checkAll(Arb.validPlayerCountToStartAGame) { playerCount ->
                val gameCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(playerCount - 1)

                Given {
                    gameCreator.`has created a game`()
                    gameCreator.invites(otherPlayers)
                    otherPlayers.each { `accept the game invite`() }
                }

                When {
                    gameCreator.`starts the game`()
                }

                Then {
                    (gameCreator + otherPlayers).each { hasGameStateWhere { hand.hasSize(1) } }
                }
            }
        }

    @Test
    @Disabled
    fun `OLD - only the player who created the game can start the game`() {
        TODO()
    }

    @Test
    fun `a game cannot be started with less than 2 players`() {
        val gameCreator = scenario.newPlayer()

        Given {
            gameCreator.`has created a game`()
        }

        When {
            gameCreator.triesTo { `starts the game`() }
        }

        Then {
            gameCreator.`gets the error`(TooFewPlayers())
        }
    }
}
