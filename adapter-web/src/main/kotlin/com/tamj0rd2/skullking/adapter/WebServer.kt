package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import org.http4k.server.Http4kServer
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsResponse
import java.net.ServerSocket

object WebServer {
    const val DEFAULT_PORT = 9000

    @JvmStatic
    fun main(args: Array<String>) {
        start(DEFAULT_PORT)
    }

    fun start(port: Int = getUnusedPort()) = createServer(application = createApp(), port = port).start()

    fun createApp(): ApplicationDomainDriver =
        ApplicationDomainDriver.create(
            gameRepository = GameRepositoryEsdbAdapter(),
        )

    fun createServer(
        application: ApplicationDomainDriver,
        port: Int = getUnusedPort(),
    ): Http4kServer {
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

        return ws.asServer(Undertow(port))
    }

    private fun getUnusedPort(): Int {
        val socket = ServerSocket(0)
        val port = socket.localPort
        socket.close()
        return port
    }
}
