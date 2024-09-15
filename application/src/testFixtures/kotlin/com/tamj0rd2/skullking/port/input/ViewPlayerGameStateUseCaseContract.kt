package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

abstract class ViewPlayerGameStateUseCaseContract {
    protected abstract fun newDriver(): ApplicationDriver

    @Test
    fun `can view the game state`() {
        val gameId = newDriver().invoke(CreateNewGameCommand).gameId

        val playersWhoJoined =
            (1..3).associate {
                val driver = newDriver()
                val playerId = driver(JoinGameCommand(gameId)).playerId
                playerId to driver
            }

        val thisPlayer = playersWhoJoined.keys.first()

        val gameState =
            playersWhoJoined.getValue(thisPlayer).invoke(
                ViewPlayerGameStateQuery(
                    gameId = gameId,
                    playerId = thisPlayer,
                ),
            )

        expectThat(gameState) {
            get { playersWhoJoined }.describedAs("players").isEqualTo(playersWhoJoined)
        }
    }
}
