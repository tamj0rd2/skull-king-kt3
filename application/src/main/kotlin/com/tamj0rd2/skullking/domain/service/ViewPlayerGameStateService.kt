package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.port.output.GameRepository

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
