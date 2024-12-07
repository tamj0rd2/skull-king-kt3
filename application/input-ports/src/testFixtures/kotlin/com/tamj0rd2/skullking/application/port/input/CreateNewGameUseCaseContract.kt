package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.players
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.listOfSize
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test
import strikt.assertions.contains

abstract class CreateNewGameUseCaseContract {
    protected abstract val scenario: TestScenario

    private val thePlayer by lazy { scenario.newPlayer() }

    @Test
    fun `the player who created the game automatically joins the game`() {
        When { thePlayer.createsAGame() }

        Then { thePlayer.`sees them self in the game`() }
    }

    @Test
    fun `the player who created the game cannot join the game a second time`() {
        Given { thePlayer.`has created a game`() }

        When { thePlayer.`tries to join the game again`() }

        Then { thePlayer.`gets the error`(PlayerHasAlreadyJoined()) }
    }

    @Test
    fun `the player who created the game can see all players who joined after them`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { playerCountWhoWillJoinAfter ->
                val gameId = thePlayer.createsAGame()

                val playersWhoJoinedAfter = listOfSize(playerCountWhoWillJoinAfter, scenario::newPlayer).each { joinsAGame(gameId) }
                thePlayer.hasGameStateWhere { players.contains(playersWhoJoinedAfter.ids) }
            }
        }
}
