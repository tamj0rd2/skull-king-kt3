package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random

class JoinGameService(
    private val gameRepository: GameRepository,
    private val gameUpdateNotifier: GameUpdateNotifier,
) : JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val playerId = PlayerId.random()

        val game = gameRepository.load(command.gameId)
        game.addPlayer(playerId).orThrow()
        gameRepository.save(game)

        gameUpdateNotifier.broadcast(GameUpdate.PlayerJoined(playerId))
        gameUpdateNotifier.subscribe(
            listener = command.gameUpdateListener,
        )

        return JoinGameOutput(playerId)
    }
}
