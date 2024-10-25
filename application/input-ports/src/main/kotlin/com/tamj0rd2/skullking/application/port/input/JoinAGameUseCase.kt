package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

interface JoinAGameUseCase {
    operator fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode>

    data class JoinGameCommand(
        val sessionId: SessionId,
        val gameId: GameId,
        val gameUpdateListener: GameUpdateListener,
    )

    data class JoinGameOutput(
        val playerId: PlayerId,
    )
}
