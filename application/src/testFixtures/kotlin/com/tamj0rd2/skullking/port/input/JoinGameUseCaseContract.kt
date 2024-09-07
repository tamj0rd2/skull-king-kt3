package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.values.ZERO
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo

abstract class JoinGameUseCaseContract {
    protected abstract val gameEventsPort: GameEventsPort
    protected abstract val useCase: JoinGameUseCase

    @Test
    fun `can join a game`() {
        val gameId = GameId.random()

        val output = useCase(JoinGameCommand(gameId))

        expectThat(output.playerId).isNotEqualTo(PlayerId.ZERO)
        expectThat(gameEventsPort.find(gameId)).isEqualTo(listOf(PlayerJoined(gameId, output.playerId)))
    }
}
