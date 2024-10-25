package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameId

interface CreateNewGameUseCase {
    operator fun invoke(command: CreateNewGameCommand): CreateNewGameOutput

    data class CreateNewGameCommand(
        val sessionId: SessionId,
    )

    data class CreateNewGameOutput(
        val gameId: GameId,
    )
}
