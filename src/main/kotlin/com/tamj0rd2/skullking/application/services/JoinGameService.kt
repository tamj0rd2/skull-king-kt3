package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.SubscribeToGameNotificationsPort
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.repositories.GameRepository
import com.tamj0rd2.skullking.domain.game.GameCommand

class JoinGameService(
    private val gameRepository: GameRepository,
    private val subscribeToGameNotificationsPort: SubscribeToGameNotificationsPort,
) : JoinGameUseCase {
    override fun execute(input: JoinGameInput): JoinGameOutput {
        subscribeToGameNotificationsPort.subscribe(input.playerId, input.receiveGameNotification)

        val (game, version) = gameRepository.load(input.gameId)
        gameRepository.save(game.execute(GameCommand.AddPlayer(input.playerId)), version)
        return JoinGameOutput
    }
}
