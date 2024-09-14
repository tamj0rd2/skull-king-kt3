package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase
import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.application.port.output.GameRepository

class ViewPlayerGameStateService(
    private val gameRepository: GameRepository,
) : ViewPlayerGameStateUseCase {
    override fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput {
        val game = gameRepository.load(query.gameId)

        return ViewPlayerGameStateOutput(
            players = game.players,
        )
    }
}
