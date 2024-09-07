package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

abstract class ViewPlayerGameStateUseCaseContract {
    protected abstract val driver: ApplicationDriver

    @Test
    fun `can view the game state`() {
        val gameId = GameId.random()
        val playersWhoJoined = (1..3).map { driver.invoke(JoinGameUseCase.JoinGameCommand(gameId)).playerId }
        val thisPlayer = playersWhoJoined.first()

        val gameState =
            driver(
                ViewPlayerGameStateQuery(
                    gameId = gameId,
                    playerId = thisPlayer,
                ),
            )

        expectThat(gameState) {
            get { players }.describedAs("players").isEqualTo(playersWhoJoined)
        }
    }
}
