package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.random

class JoinGameService(
    private val gameRepository: GameRepository,
    private val gameUpdateNotifier: GameUpdateNotifier,
) : JoinGameUseCase {
    override fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode> {
        val playerId = PlayerId.random()

        val game = gameRepository.load(command.gameId)
        game.addPlayer(playerId).onFailure { return it }
        gameRepository.save(game)

        gameUpdateNotifier.subscribe(game.id, command.gameUpdateListener)
        gameUpdateNotifier.broadcast(game.id, GameUpdate.PlayerJoined(playerId))

        return JoinGameOutput(playerId).asSuccess()
    }
}
