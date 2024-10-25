package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlayerRole.PlayerGameState.Companion.players
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameHasAlreadyStarted
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.GameIsFull
import com.tamj0rd2.skullking.domain.game.AddPlayerErrorCode.PlayerHasAlreadyJoined
import com.tamj0rd2.skullking.domain.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.listOfSize
import com.tamj0rd2.skullking.domain.game.propertyTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.isEqualTo

abstract class JoinAGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `can join a game`() {
        val player = scenario.newPlayer()
        val gameId = player.createsAGame()
        val playerId = player.joinsAGame(gameId)
        player.hasGameStateWhere { players.isEqualTo(listOf(playerId)) }
    }

    @Test
    fun `a player who joins the game is able to see themself in the game`() =
        propertyTest {
            checkAll(Arb.int(min = 1, max = MAXIMUM_PLAYER_COUNT - 1)) { playerCount ->
                val (gameId, _) = scenario.newGame(playerCount = playerCount)
                val newestPlayer = scenario.newPlayer()
                newestPlayer.joinsAGame(gameId)
                newestPlayer.hasGameStateWhere { players.contains(newestPlayer.id) }
            }
        }

    @Test
    fun `a player who joins the game is able to see every player who had already joined before them`() =
        propertyTest {
            checkAll(Arb.int(min = 1, max = MAXIMUM_PLAYER_COUNT - 1)) { playerCount ->
                val (gameId, initialPlayers) = scenario.newGame(playerCount = playerCount)
                val newestPlayer = scenario.newPlayer()
                newestPlayer.joinsAGame(gameId)
                newestPlayer.hasGameStateWhere { players.contains(initialPlayers.ids) }
            }
        }

    @Test
    fun `a player who joins the game is able to see every player who joins after them`() =
        propertyTest {
            checkAll(Arb.int(min = 1, max = MAXIMUM_PLAYER_COUNT - 1)) { playerCount ->
                val initialPlayer = scenario.newPlayer()
                val gameId = initialPlayer.createsAGame()
                initialPlayer.joinsAGame(gameId)

                val playersWhoJoinedAfter = listOfSize(playerCount, scenario::newPlayer).each { joinsAGame(gameId) }

                initialPlayer.hasGameStateWhere { players.contains(playersWhoJoinedAfter.ids) }
            }
        }

    @Test
    fun `joining a full game is not possible`() {
        val (gameId, _) = scenario.newGame(playerCount = MAXIMUM_PLAYER_COUNT)
        val anotherPlayer = scenario.newPlayer()
        expectThrows<GameIsFull> { anotherPlayer.joinsAGame(gameId) }
    }

    @Test
    fun `a player cannot join a game they're already in`() {
        val player = scenario.newPlayer()
        val gameId = player.createsAGame()
        player.joinsAGame(gameId)
        expectThrows<PlayerHasAlreadyJoined> { player.joinsAGame(gameId) }
    }

    @Test
    fun `a player cannot join a game that has already started`() =
        propertyTest {
            checkAll(Arb.int(min = MINIMUM_PLAYER_COUNT, max = MAXIMUM_PLAYER_COUNT)) { playerCount ->
                val (gameId, _) = scenario.newGame(playerCount = playerCount, startGame = true)
                val lateJoiningPlayer = scenario.newPlayer()
                expectThrows<GameHasAlreadyStarted> { lateJoiningPlayer.joinsAGame(gameId) }
            }
        }
}
