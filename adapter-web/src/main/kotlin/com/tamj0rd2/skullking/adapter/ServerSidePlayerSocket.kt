package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.values.ZERO
import org.http4k.websocket.Websocket

internal class ServerSidePlayerSocket(
    private val ws: Websocket,
    private val app: ApplicationDomainDriver,
) {
    private var gameId = GameId.NONE
        get() = field.takeIf { it != GameId.NONE } ?: error("gameId not set")

    private var playerId = PlayerId.Companion.ZERO
        get() = field.takeIf { it != PlayerId.Companion.ZERO } ?: error("playerId not set")

    fun handle(message: Message) {
        when (message) {
            is JoinGameMessage -> {
                val (gameId, playerId) = respondToJoinRequest(message)
                this.gameId = gameId
                this.playerId = playerId
            }

            is CreateNewGameMessage -> respondToCreateGameRequest()
            is GetGameStateMessage -> respondToGameStateRequest()
            else -> TODO("server received $message but has no handler")
        }
    }

    private fun respondToCreateGameRequest() {
        val output = app(CreateNewGameCommand)
        ws.send(wsLens(GameCreatedMessage(output.gameId)))
    }

    private fun respondToJoinRequest(message: JoinGameMessage): Pair<GameId, PlayerId> {
        val playerId = app(JoinGameCommand(message.gameId)).playerId
        ws.send(wsLens(JoinAcknowledgedMessage(playerId)))
        return message.gameId to playerId
    }

    private fun respondToGameStateRequest() {
        val gameState = app(ViewPlayerGameStateQuery(gameId, playerId))
        ws.send(wsLens(GameStateMessage(gameState)))
    }
}
