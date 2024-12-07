package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameUpdate
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.values.random

class CreateNewGameService(
    private val gameRepository: GameRepository,
    private val gameUpdateNotifier: GameUpdateNotifier,
    private val savePlayerIdPort: SavePlayerIdPort,
) : CreateNewGameUseCase {
    override fun invoke(command: CreateNewGameCommand): CreateNewGameOutput {
        val playerId = PlayerId.random()
        savePlayerIdPort.save(command.sessionId, playerId)

        val game = Game.new(createdBy = playerId)
        gameRepository.save(game)
        gameUpdateNotifier.subscribe(game.id, command.gameUpdateListener)
        gameUpdateNotifier.broadcast(game.id, GameUpdate.PlayerJoined(playerId))

        return CreateNewGameOutput(
            gameId = game.id,
            playerId = playerId,
        )
    }
}
