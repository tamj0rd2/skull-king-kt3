package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.listOfSize

interface TestScenario {
    fun newPlayer(): PlayerRole

    fun newPlayers(count: Int): List<PlayerRole> = listOfSize(count, ::newPlayer)

    fun newGame(
        playerCount: Int,
        startGame: Boolean = false,
    ) {
        val setup = Setup(this)
        setup.withPlayerCount(playerCount)

        if (startGame) setup.start()
    }
}

// TODO: find a way to make this possible using a property-test
class Setup(
    private val scenario: TestScenario,
) {
    private val gameCreator = scenario.newPlayer()
    private val players = mutableListOf(gameCreator)

    init {
        gameCreator.`creates a game`()
    }

    fun withPlayerCount(count: Int) =
        apply {
            repeat(count - players.size) {
                val player = scenario.newPlayer()
                gameCreator.invites(player)
                player.`accepts the game invite`()
                players.add(player)
            }
        }

    fun start() =
        apply {
            players.random().`starts the game`()
        }
}

fun List<PlayerRole>.each(block: PlayerRole.() -> Unit) = onEach(block)

val List<PlayerRole>.ids get() = map { it.id }
