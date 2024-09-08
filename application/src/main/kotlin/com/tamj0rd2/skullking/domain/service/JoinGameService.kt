package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.Player
import com.tamj0rd2.skullking.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.port.output.GameEventsPort

class JoinGameService(
    private val gameEventsPort: GameEventsPort,
) : JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val player = Player.new()
        with(gameEventsPort) { Game.addPlayer(command.gameId, player.id) }
        return JoinGameOutput(player.id)
    }
}
