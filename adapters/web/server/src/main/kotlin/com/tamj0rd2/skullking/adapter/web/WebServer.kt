package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.esdb.GameRepositoryEsdbAdapter
import com.tamj0rd2.skullking.adapter.inmemory.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.adapter.web.CreateNewGameEndpoint.sessionIdLens
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.onFailure
import org.http4k.contract.contract
import org.http4k.core.Request
import org.http4k.lens.Path
import org.http4k.routing.websockets
import org.http4k.server.Http4kServer
import org.http4k.server.PolyHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus
import java.net.ServerSocket
import org.http4k.routing.ws.bind as bindWs

object WebServer {
    private const val DEFAULT_PORT = 9000

    @JvmStatic
    fun main(
        @Suppress("unused") args: Array<String>,
    ) {
        start(DEFAULT_PORT)
    }

    fun start(port: Int = getUnusedPort()) = createServer(application = createApp(), port = port).start()

    private fun createApp(): SkullKingApplication {
        val playerIdStorage = PlayerIdStorageInMemoryAdapter()
        return SkullKingApplication(
            gameRepository = GameRepositoryEsdbAdapter(),
            gameUpdateNotifier = GameUpdateNotifierInMemoryAdapter(),
            findPlayerIdPort = playerIdStorage,
            savePlayerIdPort = playerIdStorage,
        )
    }

    fun createServer(
        application: SkullKingApplication,
        port: Int = getUnusedPort(),
    ): Http4kServer {
        val createGameController = CreateGameController(application)
        val joinGameController = JoinAGameController(application)
        val startGameController = StartGameController(application)

        val http =
            contract {
                routes += createGameController.contractRoute
            }

        val ws =
            websockets(
                "/game/{gameId}" bindWs { req: Request ->
                    val sessionId = req.sessionId
                    val gameId = GameId.parse(gameIdLens(req))

                    WsResponse { ws ->
                        ws.onError { println("server: $sessionId: error - $it") }
                        ws.onClose { println("server: $sessionId: disconnecting") }

                        val session =
                            joinGameController.joinGame(ws, sessionId, gameId).onFailure {
                                ws.send(messageToClient(ErrorMessage(it.reason)))
                                ws.close(WsStatus.REFUSE)
                                return@WsResponse
                            }

                        ws.onMessage {
                            val message = messageFromClient(it)
                            println("server: $sessionId: received $message")

                            when (message) {
                                is StartGameMessage -> startGameController(session)
                            }
                        }
                    }
                },
            )

        return PolyHandler(http, ws).asServer(Undertow(port))
    }

    private fun getUnusedPort(): Int {
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port
    }

    private val gameIdLens = Path.of("gameId")

    internal val Request.sessionId: SessionId get() = sessionIdLens.extract(this)
}

data class PlayerSession(
    val ws: Websocket,
    val gameId: GameId,
    val playerId: PlayerId,
)
