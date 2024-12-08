package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.esdb.GameRepositoryEsdbAdapter
import com.tamj0rd2.skullking.adapter.inmemory.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import org.http4k.core.Request
import org.http4k.lens.Header
import org.http4k.lens.Path
import org.http4k.routing.websockets
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus
import java.net.ServerSocket
import org.http4k.routing.ws.bind as bindWs

internal class WebServer(
    application: SkullKingApplication = createApp(),
    port: Int = getUnusedPort(),
) {
    private val createGameController = CreateGameController(application)
    private val joinGameController = JoinAGameController(application)
    private val startGameController = StartGameController(application)

    private val wsRouter =
        websockets(
            "/game" bindWs { req: Request ->
                val sessionId = req.sessionId

                WsResponse { ws ->
                    newWsResponseHandler(ws, sessionId) {
                        createGameController.createAGame(ws, sessionId)
                    }
                }
            },
            "/game/{gameId}" bindWs { req: Request ->
                val sessionId = req.sessionId
                val gameId = GameId.parse(gameIdLens(req))
                WsResponse { ws ->
                    newWsResponseHandler(ws, sessionId) {
                        joinGameController.joinGame(ws, sessionId, gameId)
                    }
                }
            },
        )

    private fun newWsResponseHandler(
        ws: Websocket,
        sessionId: SessionId,
        acquireSession: () -> Result4k<PlayerSession, GameErrorCode>,
    ) {
        println("server: $sessionId: connecting")
        ws.onError { println("server: $sessionId: error - $it") }
        ws.onClose { println("server: $sessionId: disconnecting") }

        val session =
            acquireSession().onFailure {
                ws.send(messageToClient(ErrorMessage(it.reason)))
                ws.close(WsStatus.REFUSE)
                return@newWsResponseHandler
            }

        ws.onMessage {
            val message = messageFromClient(it)
            println("server: $sessionId: received $message")

            when (message) {
                is StartGameMessage -> startGameController(session)
            }
        }

        println("server: $sessionId: connected")
    }

    private val http4kServer = wsRouter.asServer(Undertow(port))

    fun start() = http4kServer.start()

    companion object {
        private fun createApp(): SkullKingApplication {
            val playerIdStorage = PlayerIdStorageInMemoryAdapter()
            return SkullKingApplication(
                gameRepository = GameRepositoryEsdbAdapter(),
                gameUpdateNotifier = GameUpdateNotifierInMemoryAdapter(),
                findPlayerIdPort = playerIdStorage,
                savePlayerIdPort = playerIdStorage,
            )
        }

        private fun getUnusedPort(): Int {
            val socket = ServerSocket(0)
            val port = socket.localPort
            socket.close()
            return port
        }

        private val gameIdLens = Path.of("gameId")

        private val sessionIdLens =
            Header
                .map(
                    nextIn = { SessionId.parse(it) },
                    nextOut = { SessionId.show(it) },
                ).required("session_id")

        private val Request.sessionId: SessionId get() = sessionIdLens.extract(this)
    }
}

internal data class PlayerSession(
    val ws: Websocket,
    val gameId: GameId,
    val playerId: PlayerId,
)
