package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyId
import dev.forkhandles.result4k.onFailure
import org.http4k.core.Request
import org.http4k.lens.Path

internal class JoinALobbyController(
    private val joinALobbyUseCase: JoinALobbyUseCase,
) : EstablishesAPlayerSession {
    override fun establishPlayerSession(
        req: Request,
        ws: WsSession,
        lobbyNotificationListener: LobbyNotificationListener,
    ): PlayerSession {
        val lobbyId = LobbyId.parse(lobbyIdLens(req))

        val command =
            JoinALobbyCommand(
                playerId = ws.playerId,
                lobbyId = lobbyId,
                lobbyNotificationListener = lobbyNotificationListener,
            )

        val output =
            joinALobbyUseCase.invoke(command).onFailure {
                ws.send(ErrorMessage(it.reason))
                throw it.reason
            }

        ws.send(JoinAcknowledgedMessage(output.playerId))
        return PlayerSession(ws = ws, lobbyId = lobbyId, playerId = output.playerId)
    }

    companion object {
        private val lobbyIdLens = Path.of("lobbyId")
    }
}
