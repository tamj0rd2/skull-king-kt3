package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.esdb.GameRepositoryEsdbAdapter
import com.tamj0rd2.skullking.adapter.inmemory.GameUpdateNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.core.Request
import org.http4k.lens.Header
import org.http4k.routing.websockets
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.WsHandler
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
            "/game" bindWs createGameController.asWsHandler(),
            "/game/{gameId}" bindWs joinGameController.asWsHandler(),
        )

    private fun EstablishesAPlayerSession.asWsHandler(): WsHandler =
        { req: Request ->
            val sessionId = sessionIdLens.extract(req)

            WsSession.asWsResponse(sessionId) {
                val playerSession = establishPlayerSession(req, this)

                WsMessageHandler { wsMessage ->
                    when (messageFromClient(wsMessage)) {
                        is StartGameMessage -> startGameController(playerSession)
                    }
                }
            }
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

        private tailrec fun getUnusedPort(): Int =
            ServerSocket(0).use {
                val port = it.localPort
                if (port == Main.DEFAULT_PORT) return getUnusedPort()
                return port
            }

        private val sessionIdLens =
            Header
                .map(
                    nextIn = { SessionId.parse(it) },
                    nextOut = { SessionId.show(it) },
                ).required("session_id")
    }
}

internal data class PlayerSession(
    val ws: WsSession,
    val gameId: GameId,
    val playerId: PlayerId,
)

internal fun interface EstablishesAPlayerSession {
    fun establishPlayerSession(
        req: Request,
        ws: WsSession,
    ): PlayerSession
}
