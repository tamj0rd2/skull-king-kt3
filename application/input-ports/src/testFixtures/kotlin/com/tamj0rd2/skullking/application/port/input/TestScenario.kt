package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId

interface TestScenario {
    fun newPlayer(): PlayerRole

    fun newGame() = Setup(this)

    fun newGame(
        playerCount: Int,
        startGame: Boolean = false,
    ): Pair<GameId, List<PlayerRole>> {
        val setup = Setup(this)
        setup.withPlayerCount(playerCount)

        if (startGame) setup.start()
        return setup.concludeSetup()
    }
}

// TODO: find a way to make this possible using a property-test
class Setup(
    private val scenario: TestScenario,
) {
    private val players = mutableListOf<PlayerRole>()
    private val gameId: GameId

    init {
        val creator = scenario.newPlayer()
        players.add(creator)

        gameId = creator.createsAGame()
        creator.joinsAGame(gameId)
    }

    fun withEnoughPlayersToStart() = withPlayerCount(Game.MINIMUM_PLAYER_COUNT)

    fun withPlayerCount(count: Int) =
        apply {
            repeat(count - players.size) {
                val player = scenario.newPlayer()
                player.joinsAGame(gameId)
                players.add(player)
            }
        }

    fun start() =
        apply {
            players.random().startsTheGame()
        }

    fun concludeSetup() = gameId to players.toList()
}

fun List<PlayerRole>.each(block: PlayerRole.() -> Unit) = onEach(block)

val List<PlayerRole>.ids get() = map { it.id }
