package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.ErrorMessage
import com.tamj0rd2.skullking.adapter.wsLens
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import dev.forkhandles.result4k.peekFailure

class StartGameController(
    private val startGameUseCase: StartGameUseCase,
) {
    operator fun invoke(session: PlayerSession) {
        val command =
            StartGameCommand(
                gameId = session.gameId,
                playerId = session.playerId,
            )

        startGameUseCase.invoke(command).peekFailure { session.ws.send(wsLens(ErrorMessage(it))) }
    }
}
