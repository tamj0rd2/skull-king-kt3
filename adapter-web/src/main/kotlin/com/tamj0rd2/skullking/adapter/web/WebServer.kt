package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.GameRepositoryEsdbAdapter
import com.tamj0rd2.skullking.adapter.web.CreateGameController.Companion.sessionId
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.application.port.output.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameId
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
        val joinGameController = JoinGameController(application)
        val startGameController = StartGameController(application)

        val http =
            contract {
                routes += createGameController.route
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
                                ws.send(wsLens(ErrorMessage(it.reason)))
                                ws.close(WsStatus.REFUSE)
                                return@WsResponse
                            }

                        ws.onMessage {
                            val message = wsLens(it)
                            println("server: $sessionId: received $message")

                            when (message) {
                                is StartGameMessage -> startGameController(session)
                                // TODO: these will never be received by the server. it doesn't make sense for them to share a type with the above.
                                is GameCreatedMessage,
                                is GameUpdateMessage,
                                is CreateNewGameMessage,
                                is JoinAcknowledgedMessage,
                                is ErrorMessage,
                                -> error("this message should never be received by the server")
                            }
                        }
                    }
                },
            )

        return PolyHandler(
            http = http,
            ws = ws,
        ).asServer(Undertow(port))
    }

    private fun getUnusedPort(): Int {
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port
    }

    private val gameIdLens = Path.of("gameId")
}

data class PlayerSession(
    val ws: Websocket,
    val gameId: GameId,
    val playerId: PlayerId,
)
