package com.tamj0rd2.skullking.application.services

import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameOutput
import com.tamj0rd2.skullking.application.ports.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.ports.output.LoadGamePort
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort

class JoinGameService(
    private val saveGamePort: SaveGamePort,
    private val loadGamePort: LoadGamePort,
) : JoinGameUseCase {
    override fun execute(input: JoinGameInput): JoinGameOutput {
        val game = loadGamePort.load(input.gameId)!!.addPlayer(input.playerId)

        saveGamePort.save(game)

        input.receiveGameNotification.receive(GameNotification.PlayerJoined(input.playerId))
        return JoinGameOutput
    }
}
