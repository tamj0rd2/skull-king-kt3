package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase
import com.tamj0rd2.skullking.port.output.GameEventsPort

class ViewPlayerGameStateService(
    private val gameEventsPort: GameEventsPort
) : ViewPlayerGameStateUseCase {
    override fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput {
        val events = gameEventsPort.find(query.gameId)

        val game = events.fold(Game.new(query.gameId)) { game, event ->
            when (event) {
                is PlayerJoined -> game.addPlayer(event.playerId)
            }
        }

        return ViewPlayerGameStateOutput(
            players = game.players,
        )
    }
}
