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
import io.kotest.property.exhaustive.exhaustive
import org.junit.jupiter.api.Test
import strikt.api.expectThrows
import strikt.assertions.contains

abstract class JoinAGameUseCaseContract {
    protected abstract val scenario: TestScenario

    @Test
    fun `a player who joins the game can see themself in the game`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { alreadyJoinedPlayerCount ->
                val (gameId, _) = scenario.newGame(playerCount = alreadyJoinedPlayerCount)
                val newestPlayer = scenario.newPlayer()
                newestPlayer.joinsAGame(gameId)
                newestPlayer.hasGameStateWhere { players.contains(newestPlayer.id) }
            }
        }

    @Test
    fun `each player who joins the game can see players who joined before them`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { alreadyJoinedPlayerCount ->
                val (gameId, initialPlayers) = scenario.newGame(playerCount = alreadyJoinedPlayerCount)
                val newestPlayer = scenario.newPlayer()
                newestPlayer.joinsAGame(gameId)
                newestPlayer.hasGameStateWhere { players.contains(initialPlayers.ids) }
            }
        }

    @Test
    fun `each player who joins the game can see players who joined after them`() =
        propertyTest {
            checkAll((1..5).toList().exhaustive()) { alreadyJoinedPlayerCount ->
                val (gameId, initialPlayers) = scenario.newGame(playerCount = alreadyJoinedPlayerCount)

                val playerCountWhoWillJoinAfter = MAXIMUM_PLAYER_COUNT - alreadyJoinedPlayerCount
                val playersWhoWillJoinedAfter = listOfSize(playerCountWhoWillJoinAfter, scenario::newPlayer).each { joinsAGame(gameId) }

                initialPlayers.each { hasGameStateWhere { players.contains(initialPlayers.ids + playersWhoWillJoinedAfter.ids) } }
            }
        }

    @Test
    fun `cannot join a full game`() {
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
