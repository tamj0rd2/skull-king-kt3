package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import dev.forkhandles.result4k.peekFailure

internal class StartGameController(
    private val startGameUseCase: StartGameUseCase,
) {
    operator fun invoke(
        session: PlayerSession,
        message: StartGameMessage,
    ) {
        val command =
            StartGameCommand(
                lobbyId = session.lobbyId,
                playerId = session.playerId,
            )

        startGameUseCase.invoke(command).peekFailure { session.ws.send(ErrorMessage(it)) }
    }
}
