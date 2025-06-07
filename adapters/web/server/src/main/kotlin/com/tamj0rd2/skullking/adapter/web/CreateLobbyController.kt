package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyCreatedMessage
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyCommand
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.core.Request

internal class CreateLobbyController(private val createNewLobbyUseCase: CreateNewLobbyUseCase) :
    EstablishesAPlayerSession {
    override fun establishPlayerSession(
        req: Request,
        sendAMessage: SendAMessage,
        playerId: PlayerId,
        lobbyNotificationListener: LobbyNotificationListener,
    ): LobbyId {
        val command =
            CreateNewLobbyCommand(
                playerId = playerId,
                lobbyNotificationListener = lobbyNotificationListener,
            )

        val createLobbyOutput = createNewLobbyUseCase(command)
        sendAMessage(LobbyCreatedMessage(createLobbyOutput.lobbyId))
        return createLobbyOutput.lobbyId
    }
}
