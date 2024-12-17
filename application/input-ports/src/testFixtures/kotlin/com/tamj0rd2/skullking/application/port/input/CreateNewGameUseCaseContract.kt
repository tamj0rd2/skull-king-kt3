package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test

abstract class CreateNewGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `the player who created the game automatically joins the game`() {
        val theGameCreator = scenario.newPlayer()

        When { theGameCreator.`creates a game`() }

        Then { theGameCreator.`sees them self in the game`() }
    }

    @Test
    fun `the player who created the game cannot join the game a second time`() {
        val theGameCreator = scenario.newPlayer()

        Given { theGameCreator.`has created a game`() }

        When { theGameCreator.triesTo { `join the game again`() } }

        Then { theGameCreator.`gets the error`(PlayerHasAlreadyJoined()) }
    }

    @Test
    fun `the player who created the game can see all players who joined after them`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { additionalPlayerCount ->
                val theGameCreator = scenario.newPlayer()
                val otherPlayers = scenario.newPlayers(additionalPlayerCount)

                Given {
                    theGameCreator.`has created a game`()
                    theGameCreator.invites(otherPlayers)
                }

                When {
                    otherPlayers.each { `accept the game invite`() }
                }

                Then {
                    theGameCreator.`sees each invited player in the game`()
                }
            }
        }
}
