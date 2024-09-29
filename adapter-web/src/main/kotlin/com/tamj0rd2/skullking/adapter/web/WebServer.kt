package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.GameRepositoryEsdbAdapter
import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifierInMemoryAdapter
import org.http4k.contract.contract
import org.http4k.core.Request
import org.http4k.routing.websockets
import org.http4k.server.Http4kServer
import org.http4k.server.PolyHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer
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

        val http =
            contract {
                routes += createGameController.route
            }

        val ws =
            websockets(
                "/game/{gameId}" bindWs { req: Request ->
                    WsResponse { ws ->
                        ws.onMessage { println("server: received ${it.body}") }
                        ws.onError { println("server: error: $it") }
                        ws.onClose { println("server: client is disconnecting") }

                        joinGameController.joinGame(req, ws)
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
}
