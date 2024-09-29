package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.GameRepositoryEsdbAdapter
import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
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

    private fun createApp(): ApplicationDomainDriver =
        ApplicationDomainDriver(
            gameRepository = GameRepositoryEsdbAdapter(),
            // TODO: swap this out maybe?
            gameUpdateNotifier = GameUpdateNotifierInMemoryAdapter(),
        )

    fun createServer(
        application: ApplicationDomainDriver,
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
                    // TODO: send the client ID (or session id?) in the header when connecting to ws.
                    val clientId = newClientId()
                    val gameId = GameId.parse(gameIdLens(req))

                    WsResponse { ws ->
                        ws.onError { println("server: client $clientId: error - $it") }
                        ws.onClose { println("server: client $clientId: disconnecting") }

                        val playerId = joinGameController.joinGame(ws, gameId)
                        val session = PlayerSession(ws = ws, gameId = gameId, playerId = playerId)

                        ws.onMessage {
                            val message = wsLens(it)
                            println("server: client $clientId: received $message")

                            when (message) {
                                is StartGameMessage -> startGameController(session)
                                // TODO: these will never be received by the server. it doesn't make sense for them to share a type with the above.
                                is GameCreatedMessage,
                                is GameUpdateMessage,
                                is CreateNewGameMessage,
                                is JoinAcknowledgedMessage,
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

    private var clientCount = 0

    private fun newClientId(): Int = ++clientCount

    private val gameIdLens = Path.of("gameId")
}

data class PlayerSession(
    val ws: Websocket,
    val gameId: GameId,
    val playerId: PlayerId,
)
