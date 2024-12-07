package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.players
import com.tamj0rd2.skullking.domain.game.listOfSize
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.assertions.contains
import strikt.assertions.isSuccess

abstract class CreateNewGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `can create a new game`() {
        val player = scenario.newPlayer()
        expectCatching { player.createsAGame() }.isSuccess()
    }

    @Test
    fun `the player who created the game can see them self in the game`() =
        propertyTest {
            val player = scenario.newPlayer()
            val gameId = player.createsAGame()
            player.joinsAGame(gameId)
            player.hasGameStateWhere { players.contains(player.id) }
        }

    @Test
    fun `the player who created the game can see all players who joined after them`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { playerCountWhoWillJoinAfter ->
                val initialPlayer = scenario.newPlayer()
                val gameId = initialPlayer.createsAGame()
                initialPlayer.joinsAGame(gameId)

                val playersWhoJoinedAfter = listOfSize(playerCountWhoWillJoinAfter, scenario::newPlayer).each { joinsAGame(gameId) }
                initialPlayer.hasGameStateWhere { players.contains(playersWhoJoinedAfter.ids) }
            }
        }
}
