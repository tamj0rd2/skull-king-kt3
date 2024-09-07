package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains

abstract class JoinGameUseCaseContract {
    protected abstract val driver: ApplicationDriver

    @Test
    fun `can join a game`() {
        val gameId = GameId.random()

        val playerId = driver(JoinGameCommand(gameId)).playerId
        val gameState = driver(ViewPlayerGameStateQuery(gameId, playerId))

        expectThat(gameState.players).contains(playerId)
    }
}
