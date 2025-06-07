package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.onFailure
import org.http4k.core.Request
import org.http4k.lens.Path

internal class JoinALobbyController(private val joinALobbyUseCase: JoinALobbyUseCase) :
    EstablishesAPlayerSession {
    override fun establishPlayerSession(
        req: Request,
        sendAMessage: SendAMessage,
        playerId: PlayerId,
        lobbyNotificationListener: LobbyNotificationListener,
    ): LobbyId {
        val lobbyId = LobbyId.parse(lobbyIdLens(req))

        val command =
            JoinALobbyCommand(
                playerId = playerId,
                lobbyId = lobbyId,
                lobbyNotificationListener = lobbyNotificationListener,
            )

        joinALobbyUseCase.invoke(command).onFailure {
            sendAMessage(ErrorMessage(it.reason))
            throw it.reason
        }

        sendAMessage(JoinAcknowledgedMessage)
        return lobbyId
    }

    companion object {
        private val lobbyIdLens = Path.of("lobbyId")
    }
}
