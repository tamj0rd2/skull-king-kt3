package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.ApplicationDomainDriver
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.values.ZERO
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsResponse

object WebServer {
    @JvmStatic
    fun main(args: Array<String>) {
        val app =
            ApplicationDomainDriver.create(
                gameEventsPort = GameEventsDummyAdapter(),
            )
        createServer(app).start()
    }

    fun createServer(application: ApplicationDomainDriver): Http4kServer {
        val ws: WsHandler = {
            WsResponse { ws: Websocket ->
                val playerSocket = ServerSidePlayerSocket(ws, application)
                ws.onMessage {
                    println("server: received ${it.body}")
                    playerSocket.handle(wsLens(it))
                }
                ws.onError { println("server: error: $it") }
                ws.onClose { println("server: client is disconnecting") }
            }
        }

        return ws.asServer(Undertow(9000))
    }
}

private class ServerSidePlayerSocket(
    private val ws: Websocket,
    private val app: ApplicationDomainDriver,
) {
    var gameId = GameId.ZERO
        get() = field.takeIf { it != GameId.ZERO } ?: error("gameId not set")

    var playerId = PlayerId.ZERO
        get() = field.takeIf { it != PlayerId.ZERO } ?: error("playerId not set")

    fun handle(message: Message) {
        when (message) {
            is JoinGameMessage -> {
                val (gameId, playerId) = handle(message)
                this.gameId = gameId
                this.playerId = playerId
            }

            is GetGameStateMessage -> {
                val gameState = app(ViewPlayerGameStateQuery(gameId, playerId))
                ws.send(wsLens(GameStateMessage(gameState)))
            }

            else -> TODO("server received $message but has no handler")
        }
    }

    private fun handle(message: JoinGameMessage): Pair<GameId, PlayerId> {
        val playerId = app(JoinGameCommand(message.gameId)).playerId
        ws.send(wsLens(JoinAcknowledgedMessage(playerId)))
        return message.gameId to playerId
    }
}

class GameEventsDummyAdapter : GameEventsPort {
    override fun findGameEvents(gameId: GameId): List<GameEvent> {
        TODO("Not yet implemented")
    }

    override fun saveGameEvents(events: List<GameEvent>) {
        TODO("Not yet implemented")
    }
}
