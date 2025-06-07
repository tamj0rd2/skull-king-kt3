package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

internal class StartGameController(private val startGameUseCase: StartGameUseCase) :
    MessageReceiver<StartGameMessage> {
    override fun receive(
        playerId: PlayerId,
        lobbyId: LobbyId,
        message: StartGameMessage,
    ): Result4k<*, LobbyErrorCode> {
        val command = StartGameCommand(lobbyId = lobbyId, playerId = playerId)

        return startGameUseCase.invoke(command)
    }
}
