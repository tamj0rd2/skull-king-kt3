package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.GameUpdate
import org.junit.jupiter.api.Test

abstract class StartGameUseCaseContract : GameUseCaseContract {
    @Test
    fun `when the minimum number of players required for a game have joined, the game can be started`() {
        // TODO: turn this into a property test

        val player1 = newPlayerRole()
        val player2 = newPlayerRole()

        val gameId = player1.createsAGame()
        player1.joinsAGame(gameId)
        player2.joinsAGame(gameId)
        player1.startsTheGame()

        player2.received(GameUpdate.GameStarted)
        // TODO: what else does it mean for a game to be started? Probably:
        //  - that round one has started
        //  - it's the bidding phase
        //  - each player has 1 card
        //  - within the players hands, there can't be more cards than exist of that type (new cards aren't invented from thin air)
        //  - bidding is possible when in this state.
    }

    // TODO: only the player who created the game can start the game
    // TODO: the game cannot be started when there are less than 2 players
}
