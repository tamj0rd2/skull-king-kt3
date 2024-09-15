package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.values.ZERO
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isNotEqualTo

abstract class JoinGameUseCaseContract {
    protected abstract fun newDriver(): ApplicationDriver

    @Test
    fun `can join a game`() {
        val driver = newDriver()
        val gameId = driver(CreateNewGameCommand).gameId

        val playerId = driver(JoinGameCommand(gameId)).playerId
        expectThat(playerId).isNotEqualTo(PlayerId.ZERO)

        val gameState = driver(ViewPlayerGameStateQuery(gameId, playerId))
        expectThat(gameState.players).contains(playerId)
    }
}
