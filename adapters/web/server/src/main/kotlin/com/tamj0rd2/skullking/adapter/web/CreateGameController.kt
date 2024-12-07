package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.adapter.web.CreateNewGameEndpoint.GameCreatedMessage
import com.tamj0rd2.skullking.adapter.web.JoinAGameController.Companion.newGameUpdateListener
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import dev.forkhandles.result4k.Result4k
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.websocket.Websocket

class CreateGameController(
    private val createNewGameUseCase: CreateNewGameUseCase,
) {
    val contractRoute = CreateNewGameEndpoint.contract to ::handle

    fun createAGame(
        ws: Websocket,
        sessionId: SessionId,
    ): Result4k<PlayerSession, GameErrorCode> {
        val command =
            CreateNewGameCommand(
                sessionId = sessionId,
                gameUpdateListener = newGameUpdateListener(ws),
            )

        val (gameId, playerId) = createNewGameUseCase(command)
        ws.send(messageToClient(GameCreatedMessage(gameId, playerId)))
        return PlayerSession(ws, gameId, playerId).asSuccess()
    }

    private fun handle(request: Request): Response {
        TODO("http thing is going away.")
    }
}
