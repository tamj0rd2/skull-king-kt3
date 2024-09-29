package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate
import com.tamj0rd2.skullking.domain.model.PlayerId
import org.http4k.core.Request
import org.http4k.lens.Path
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

class JoinGameController(
    private val joinGameUseCase: JoinGameUseCase,
) {
    fun joinGame(
        request: Request,
        ws: Websocket,
    ): PlayerId {
        val command =
            JoinGameCommand(
                gameId = request.gameId,
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
        private val gameIdLens = Path.of("gameId")
        private val Request.gameId get() = GameId.parse(gameIdLens(this))

        private fun GameUpdate.toMessage(): WsMessage = wsLens(GameUpdateMessage(this))
    }
}
