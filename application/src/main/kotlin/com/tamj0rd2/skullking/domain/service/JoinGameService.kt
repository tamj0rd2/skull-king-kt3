package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.domain.model.Player
import com.tamj0rd2.skullking.domain.repository.GameRepository
import com.tamj0rd2.skullking.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameOutput
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
