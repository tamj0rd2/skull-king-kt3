package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameId

interface TestScenario {
    fun newPlayer(): PlayerRole

    fun newGame() = Setup(this)
}

// TODO: find a way to make this possible using a property-test
class Setup(
    private val scenario: TestScenario,
) {
    private val playerRoles = mutableListOf<PlayerRole>()
    private val gameId: GameId

    init {
        val creator = scenario.newPlayer()
        playerRoles.add(creator)

        gameId = creator.createsAGame()
        creator.joinsAGame(gameId)
    }

    fun withMinimumPlayersToStart() = withPlayerCount(Game.MINIMUM_PLAYER_COUNT)

    fun withPlayerCount(count: Int) =
        apply {
            repeat(count - playerRoles.size) {
                val player = scenario.newPlayer()
                player.joinsAGame(gameId)
                playerRoles.add(player)
            }
        }

    fun done() = gameId to playerRoles.toList()
}

fun List<PlayerRole>.each(block: PlayerRole.() -> Unit) = forEach(block)
