package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.adapter.web.MessageToClient.GameUpdateMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.GameUpdate
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage

internal class JoinAGameController(
    private val joinAGameUseCase: JoinAGameUseCase,
) {
    fun joinGame(
        ws: Websocket,
        sessionId: SessionId,
        gameId: GameId,
    ): Result4k<PlayerSession, GameErrorCode> {
        val command =
            JoinGameCommand(
                sessionId = sessionId,
                gameId = gameId,
                gameUpdateListener = newGameUpdateListener(ws),
            )

        val output = joinAGameUseCase.invoke(command).onFailure { return it }
        ws.send(messageToClient(JoinAcknowledgedMessage(output.playerId)))
        return PlayerSession(ws = ws, gameId = gameId, playerId = output.playerId).asSuccess()
    }

    companion object {
        internal fun newGameUpdateListener(ws: Websocket) =
            GameUpdateListener { updates ->
                updates
                    .map { it.toMessage() }
                    .forEach { ws.send(it) }
            }

        private fun GameUpdate.toMessage(): WsMessage = messageToClient(GameUpdateMessage(this))
    }
}
