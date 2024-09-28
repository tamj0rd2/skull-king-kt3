package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId

interface JoinGameUseCase {
    operator fun invoke(command: JoinGameCommand): JoinGameOutput

    data class JoinGameCommand(
        val gameId: GameId,
        val gameUpdateListener: GameUpdateListener,
    )

    data class JoinGameOutput(
        val playerId: PlayerId,
    )
}
