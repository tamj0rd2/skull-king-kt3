package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId

interface StartGameUseCase {
    operator fun invoke(command: StartGameCommand): StartGameOutput

    data class StartGameCommand(
        val gameId: GameId,
        val playerId: PlayerId,
    )

    data object StartGameOutput
}
