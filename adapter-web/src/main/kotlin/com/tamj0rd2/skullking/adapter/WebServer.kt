package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.GameUpdate
import org.http4k.contract.contract
import org.http4k.core.Request
import org.http4k.lens.Path
import org.http4k.routing.websockets
import org.http4k.server.Http4kServer
import org.http4k.server.PolyHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.WsMessage
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
        val http =
            contract {
                routes += CreateGameController(application).route
            }

        val gameIdLens = Path.of("gameId")

        val ws =
            websockets(
                "/game/{gameId}" bindWs { req: Request ->
                    val gameId = GameId.parse(gameIdLens(req))

                    // TODO: this code needs organising.
                    WsResponse { ws ->
                        ws.onMessage { println("server: received ${it.body}") }
                        ws.onError { println("server: error: $it") }
                        ws.onClose { println("server: client is disconnecting") }

                        val listenerForThisPlayer =
                            object : GameUpdateListener {
                                override fun send(updates: List<GameUpdate>) {
                                    updates
                                        .map { it.toMessage() }
                                        .forEach { ws.send(it) }
                                }
                            }

                        val playerId = application(JoinGameCommand(gameId, listenerForThisPlayer)).playerId
                        ws.send(wsLens(JoinAcknowledgedMessage(playerId)))
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

    private fun GameUpdate.toMessage(): WsMessage = wsLens(GameUpdateMessage(this))
}
