package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.application.port.input.testsupport.each
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test

interface CreateNewLobbyUseCaseContract : UseCaseContract {
    @Test
    fun `the player who created the lobby automatically joins the lobby`() {
        val theLobbyCreator = scenario.newPlayer()

        When { theLobbyCreator.`creates a lobby`() }

        Then { theLobbyCreator.`sees them self in the lobby`() }
    }

    @Test
    fun `the player who created the lobby cannot join the lobby a second time`() {
        val theLobbyCreator = scenario.newPlayer()

        Given { theLobbyCreator.`has created a lobby`() }

        When { theLobbyCreator.triesTo { `join the lobby again`() } }

        Then { theLobbyCreator.`gets the error`(PlayerHasAlreadyJoined()) }
    }

    @Test
    fun `the player who created the lobby can see all players who joined after them`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { additionalPlayerCount ->
                val theLobbyCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(additionalPlayerCount)

                Given {
                    theLobbyCreator.`has created a lobby`()
                    theLobbyCreator.invites(otherPlayers)
                }

                When {
                    otherPlayers.each { `accept the lobby invite`() }
                }

                Then {
                    theLobbyCreator.`sees each invited player in the lobby`()
                }
            }
        }
}
