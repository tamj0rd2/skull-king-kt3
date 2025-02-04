package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyCreatedMessage
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyCommand
import org.http4k.core.Request

internal class CreateLobbyController(
    private val createNewLobbyUseCase: CreateNewLobbyUseCase,
) : EstablishesAPlayerSession {
    override fun establishPlayerSession(
        req: Request,
        ws: WsSession,
        lobbyNotificationListener: LobbyNotificationListener,
    ): PlayerSession {
        val command =
            CreateNewLobbyCommand(
                sessionId = ws.sessionId,
                playerId = ws.playerId,
                lobbyNotificationListener = lobbyNotificationListener,
            )

        val (lobbyId, playerId) = createNewLobbyUseCase(command)
        ws.send(LobbyCreatedMessage(lobbyId, playerId))
        return PlayerSession(ws, lobbyId, playerId)
    }
}
