package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameId
import dev.forkhandles.result4k.Result4k

interface JoinGameUseCase {
    operator fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode>

    data class JoinGameCommand(
        val gameId: GameId,
        val gameUpdateListener: GameUpdateListener,
    )

    data class JoinGameOutput(
        val playerId: PlayerId,
    )
}
