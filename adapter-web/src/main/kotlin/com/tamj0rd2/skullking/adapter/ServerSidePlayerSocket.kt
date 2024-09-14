package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.ApplicationDomainDriver
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase
import dev.forkhandles.values.ZERO
import org.http4k.websocket.Websocket

internal class ServerSidePlayerSocket(
    private val ws: Websocket,
    private val app: ApplicationDomainDriver,
) {
    var gameId = GameId.Companion.ZERO
        get() = field.takeIf { it != GameId.Companion.ZERO } ?: error("gameId not set")

    var playerId = PlayerId.Companion.ZERO
        get() = field.takeIf { it != PlayerId.Companion.ZERO } ?: error("playerId not set")

    fun handle(message: Message) {
        when (message) {
            is JoinGameMessage -> {
                val (gameId, playerId) = respondToJoinRequest(message)
                this.gameId = gameId
                this.playerId = playerId
            }

            is GetGameStateMessage -> respondToGameStateRequest()
            else -> TODO("server received $message but has no handler")
        }
    }

    private fun respondToJoinRequest(message: JoinGameMessage): Pair<GameId, PlayerId> {
        val playerId = app(JoinGameUseCase.JoinGameCommand(message.gameId)).playerId
        ws.send(wsLens(JoinAcknowledgedMessage(playerId)))
        return message.gameId to playerId
    }

    private fun respondToGameStateRequest() {
        val gameState = app(ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery(gameId, playerId))
        ws.send(wsLens(GameStateMessage(gameState)))
    }
}
