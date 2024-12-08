package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.JoinAGameController.Companion.newGameUpdateListener
import com.tamj0rd2.skullking.adapter.web.MessageToClient.GameCreatedMessage
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import org.http4k.core.Request

internal class CreateGameController(
    private val createNewGameUseCase: CreateNewGameUseCase,
) : EstablishesAPlayerSession {
    override fun establishPlayerSession(
        req: Request,
        ws: WsSession,
    ): PlayerSession {
        val command =
            CreateNewGameCommand(
                sessionId = ws.sessionId,
                gameUpdateListener = newGameUpdateListener(ws),
            )

        val (gameId, playerId) = createNewGameUseCase(command)
        ws.send(GameCreatedMessage(gameId, playerId))
        return PlayerSession(ws, gameId, playerId)
    }
}
