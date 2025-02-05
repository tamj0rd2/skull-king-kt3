package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.peekFailure

internal class StartGameController(
    private val startGameUseCase: StartGameUseCase,
) : MessageReceiver<StartGameMessage> {
    override fun receive(
        ws: MessageSender,
        playerId: PlayerId,
        lobbyId: LobbyId,
        message: StartGameMessage,
    ) {
        val command =
            StartGameCommand(
                lobbyId = lobbyId,
                playerId = playerId,
            )

        startGameUseCase.invoke(command).peekFailure { ws.send(ErrorMessage(it)) }
    }
}
