package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import com.tamj0rd2.skullking.port.input.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameOutput
import com.tamj0rd2.skullking.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.values.random

class JoinGameService(
    private val gameEventsPort: GameEventsPort,
): JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val playerId = PlayerId.random()
        gameEventsPort.save(PlayerJoined(command.gameId, playerId))
        return JoinGameOutput(playerId)
    }
}
