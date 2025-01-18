package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.output.FindPlayerIdPort
import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.application.port.output.SavePlayerIdPort
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameAction
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.random

class JoinAGameService(
    private val gameRepository: GameRepository,
    private val gameUpdateNotifier: GameUpdateNotifier,
    private val findPlayerIdPort: FindPlayerIdPort,
    private val savePlayerIdPort: SavePlayerIdPort,
) : JoinAGameUseCase {
    override fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode> {
        val playerId = findOrCreatePlayerId(command.sessionId)

        val game = gameRepository.load(command.gameId)
        game.execute(GameAction.AddPlayer(playerId)).onFailure { return it }
        gameRepository.save(game)

        gameUpdateNotifier.subscribe(game.id, command.gameUpdateListener)
        gameUpdateNotifier.broadcast(game.id, GameUpdate.APlayerHasJoined(playerId))

        return JoinGameOutput(playerId).asSuccess()
    }

    private fun findOrCreatePlayerId(sessionId: SessionId): PlayerId =
        findPlayerIdPort.findBy(sessionId)
            ?: PlayerId.random().also { savePlayerIdPort.save(sessionId, it) }
}
