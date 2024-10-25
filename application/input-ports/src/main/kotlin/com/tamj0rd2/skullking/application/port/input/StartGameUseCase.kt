package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameId
import dev.forkhandles.result4k.Result4k

interface StartGameUseCase {
    operator fun invoke(command: StartGameCommand): Result4k<StartGameOutput, GameErrorCode>

    data class StartGameCommand(
        val gameId: GameId,
        val playerId: PlayerId,
    )

    data object StartGameOutput
}
