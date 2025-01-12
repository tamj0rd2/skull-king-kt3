package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.inandout.GameUpdateListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId

interface CreateNewGameUseCase {
    operator fun invoke(command: CreateNewGameCommand): CreateNewGameOutput

    data class CreateNewGameCommand(
        val sessionId: SessionId,
        val gameUpdateListener: GameUpdateListener,
    )

    data class CreateNewGameOutput(
        val gameId: GameId,
        val playerId: PlayerId,
    )
}
