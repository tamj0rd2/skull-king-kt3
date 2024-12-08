package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.GameUpdateMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.game.GameId
import dev.forkhandles.result4k.onFailure
import org.http4k.core.Request
import org.http4k.lens.Path

internal class JoinAGameController(
    private val joinAGameUseCase: JoinAGameUseCase,
) : EstablishesAPlayerSession {
    override fun establishPlayerSession(
        req: Request,
        ws: WsSession,
    ): PlayerSession {
        val gameId = GameId.parse(gameIdLens(req))

        val command =
            JoinGameCommand(
                sessionId = ws.sessionId,
                gameId = gameId,
                gameUpdateListener = newGameUpdateListener(ws),
            )

        val output =
            joinAGameUseCase.invoke(command).onFailure {
                ws.send(ErrorMessage(it.reason))
                throw it.reason
            }

        ws.send(JoinAcknowledgedMessage(output.playerId))
        return PlayerSession(ws = ws, gameId = gameId, playerId = output.playerId)
    }

    companion object {
        // TODO: put this somewhere properly shared?
        internal fun newGameUpdateListener(ws: WsSession) =
            GameUpdateListener { updates ->
                updates.map(::GameUpdateMessage).forEach(ws::send)
            }

        // TODO: how can I use this as part of the path in WebServer.kt?
        private val gameIdLens = Path.of("gameId")
    }
}
