package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.domain.model.GameId
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.routing.routes
import org.http4k.routing.websockets
import org.http4k.server.Http4kServer
import org.http4k.server.PolyHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.WsResponse
import java.net.ServerSocket
import org.http4k.routing.bind as bindHttp
import org.http4k.routing.ws.bind as bindWs

object WebServer {
    const val DEFAULT_PORT = 9000

    @JvmStatic
    fun main(args: Array<String>) {
        start(DEFAULT_PORT)
    }

    fun start(port: Int = getUnusedPort()) = createServer(application = createApp(), port = port).start()

    fun createApp(): ApplicationDomainDriver =
        ApplicationDomainDriver(
            gameRepository = GameRepositoryEsdbAdapter(),
        )

    fun createServer(
        application: ApplicationDomainDriver,
        port: Int = getUnusedPort(),
    ): Http4kServer {
        val http =
            routes(
                // TODO: define this using http4k contract
                "/game" bindHttp Method.POST to {
                    val output = application(CreateNewGameCommand)
                    val message = GameCreatedMessage(output.gameId)
                    Response(Status.CREATED).with(httpLens of message)
                },
            )

        val gameIdLens = Path.of("gameId")

        val ws =
            websockets(
                "/game/{gameId}" bindWs { req: Request ->
                    val gameId = GameId.parse(gameIdLens(req))
                    val playerId = application(JoinGameCommand(gameId)).playerId

                    WsResponse { ws ->
                        ws.send(wsLens(JoinAcknowledgedMessage(playerId)))

                        val playerSocket = ServerSidePlayerSocket(ws, application)
                        ws.onMessage {
                            println("server: received ${it.body}")
                            playerSocket.handle(wsLens(it))
                        }
                        ws.onError { println("server: error: $it") }
                        ws.onClose { println("server: client is disconnecting") }
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
