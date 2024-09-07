package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId

interface JoinGameUseCase {
    operator fun invoke(command: JoinGameCommand): JoinGameOutput

    data class JoinGameCommand(
        val gameId: GameId,
    )

    data class JoinGameOutput(
        val playerId: PlayerId,
    )

}
