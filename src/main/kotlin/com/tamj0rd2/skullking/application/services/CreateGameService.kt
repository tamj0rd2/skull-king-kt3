package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.CreateGameOutput
import com.tamj0rd2.skullking.application.ports.input.CreateGameUseCase
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId
import dev.forkhandles.values.random

class CreateGameService(private val saveGamePort: SaveGamePort) : CreateGameUseCase {
    override fun execute(input: CreateGameInput): CreateGameOutput {
        val game = Game.new(GameId.random(), input.playerId)
        saveGamePort.save(game)
        return CreateGameOutput(game.id)
    }
}
