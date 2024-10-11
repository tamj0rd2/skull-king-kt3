package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

class JoinGameController(
    private val joinGameUseCase: JoinGameUseCase,
) {
    fun joinGame(
        ws: Websocket,
        gameId: GameId,
    ): PlayerId {
        val command =
            JoinGameCommand(
                gameId = gameId,
                gameUpdateListener = newGameUpdateListener(ws),
            )

        val output = joinGameUseCase.invoke(command)
        ws.send(wsLens(JoinAcknowledgedMessage(output.playerId)))
        return output.playerId
    }

    private fun newGameUpdateListener(ws: Websocket) =
        GameUpdateListener { updates ->
            updates
                .map { it.toMessage() }
                .forEach { ws.send(it) }
        }

    companion object {
        private fun GameUpdate.toMessage(): WsMessage = wsLens(GameUpdateMessage(this))
    }
}
