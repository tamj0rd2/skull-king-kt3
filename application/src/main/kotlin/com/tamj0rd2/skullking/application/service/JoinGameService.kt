package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.model.Player
import dev.forkhandles.result4k.orThrow

class JoinGameService(
    private val gameRepository: GameRepository,
) : JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val player = Player.new()
        with(gameRepository) {
            val game = load(command.gameId)
            game.addPlayer(player.id).orThrow()
            save(game)
        }
        return JoinGameOutput(player.id)
    }
}
