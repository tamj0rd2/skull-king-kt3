package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole.PlayerLobbyState.Companion.hand
import com.tamj0rd2.skullking.application.port.input.testsupport.PlayerRole.PlayerLobbyState.Companion.roundNumber
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.application.port.input.testsupport.each
import com.tamj0rd2.skullking.domain.game.LobbyArbs.validPlayerCountToStartAGame
import com.tamj0rd2.skullking.domain.game.RoundNumber
import com.tamj0rd2.skullking.domain.game.StartGameErrorCode.TooFewPlayers
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

interface StartGameUseCaseContract : UseCaseContract {
    val propertyTestIterations: Int
        get() = 1000

    @Test
    fun `starting the game begins round 1`() {
        propertyTest {
            checkAll(propertyTestIterations, Arb.validPlayerCountToStartAGame) { playerCount ->
                val gameCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(playerCount - 1)

                Given {
                    gameCreator.`has created a lobby`()
                    gameCreator.invites(otherPlayers)
                    otherPlayers.each { `accept the lobby invite`() }
                }

                When { gameCreator.`starts the game`() }

                Then {
                    (gameCreator + otherPlayers).each {
                        hasLobbyStateWhere { roundNumber.isEqualTo(RoundNumber.of(1)) }
                    }
                }
            }
        }
    }

    @Test
    fun `each player is dealt 1 card`() = propertyTest {
        checkAll(propertyTestIterations, Arb.validPlayerCountToStartAGame) { playerCount ->
            val gameCreator = scenario.newPlayer()
            val otherPlayers = scenario.newPlayers(playerCount - 1)

            Given {
                gameCreator.`has created a lobby`()
                gameCreator.invites(otherPlayers)
                otherPlayers.each { `accept the lobby invite`() }
            }

            When { gameCreator.`starts the game`() }

            Then { (gameCreator + otherPlayers).each { hasLobbyStateWhere { hand.hasSize(1) } } }
        }
    }

    @Test
    @Disabled
    fun `only the player who created the lobby can start the game`() {
        TODO()
    }

    @Test
    fun `a game cannot be started with less than 2 players`() {
        val gameCreator = scenario.newPlayer()

        Given { gameCreator.`has created a lobby`() }

        When { gameCreator.triesTo { `starts the game`() } }

        Then { gameCreator.`gets the error`(TooFewPlayers()) }
    }
}
