package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.SubscribeToGameNotificationsPort
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.CreateGameOutput
import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort
import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.Game

class CreateGameService(
    private val saveGamePort: SaveGamePort,
    private val subscribeToGameNotificationsPort: SubscribeToGameNotificationsPort,
) : CreateGameUseCase {
    override fun execute(input: CreateGameInput): CreateGameOutput {
        subscribeToGameNotificationsPort.subscribe(input.playerId, input.receiveGameNotification)

        val game = Game.new(input.playerId)
        saveGamePort.save(game, Version.initial)
        return CreateGameOutput
    }
}
