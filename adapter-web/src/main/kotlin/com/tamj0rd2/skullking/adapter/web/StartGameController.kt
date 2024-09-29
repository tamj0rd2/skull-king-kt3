package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand

class StartGameController(
    private val startGameUseCase: StartGameUseCase,
) {
    operator fun invoke(session: PlayerSession) {
        val command =
            StartGameCommand(
                gameId = session.gameId,
                playerId = session.playerId,
            )

        startGameUseCase.invoke(command)
    }
}
