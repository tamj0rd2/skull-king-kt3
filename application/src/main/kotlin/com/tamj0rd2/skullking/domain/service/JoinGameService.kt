package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.Player
import com.tamj0rd2.skullking.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.result4k.orThrow

class JoinGameService(
    private val gameEventsPort: GameEventsPort,
) : JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val player = Player.new()
        with(gameEventsPort) {
            val game = Game(command.gameId, findGameEvents(command.gameId))
            game.addPlayer(player.id).orThrow()
            saveGameEvents(game.changes)
        }
        return JoinGameOutput(player.id)
    }
}
