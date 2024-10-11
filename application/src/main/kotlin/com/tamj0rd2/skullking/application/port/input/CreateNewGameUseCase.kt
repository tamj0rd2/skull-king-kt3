package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.game.GameId

interface CreateNewGameUseCase {
    operator fun invoke(command: CreateNewGameCommand): CreateNewGameOutput

    data object CreateNewGameCommand

    data class CreateNewGameOutput(
        val gameId: GameId,
    )
}
