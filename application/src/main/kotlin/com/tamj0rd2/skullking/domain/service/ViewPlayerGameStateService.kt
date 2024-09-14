package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.port.output.GameEventsPort

class ViewPlayerGameStateService(
    private val gameEventsPort: GameEventsPort,
) : ViewPlayerGameStateUseCase {
    override fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput {
        val game = Game(query.gameId, gameEventsPort.findGameEvents(query.gameId))

        return ViewPlayerGameStateOutput(
            players = game.players,
        )
    }
}
