package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.esdb.EventStoreEsdbAdapter
import com.tamj0rd2.skullking.adapter.esdb.EventStoreEsdbAdapter.StreamNameProvider
import com.tamj0rd2.skullking.adapter.inmemory.LobbyNotifierInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyNotificationMessage
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.serialization.json.JLobbyEvent
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
    private val createLobbyController = CreateLobbyController(application)
    private val joinALobbyController = JoinALobbyController(application)
    private val startGameController = StartGameController(application)
    private val placeABidController = PlaceABidController(application)
    private val playACardController = PlayACardController(application)

    private val wsRouter =
        websockets(
            "/game" bindWs createLobbyController.asWsHandler(),
            "/game/{lobbyId}" bindWs joinALobbyController.asWsHandler(),
        )

    private fun EstablishesAPlayerSession.asWsHandler(): WsHandler =
        { req: Request ->
            val sessionId = sessionIdLens.extract(req)

            WsSession.asWsResponse(sessionId) {
                val playerSession =
                    establishPlayerSession(
                        req = req,
                        ws = this,
                        lobbyNotificationListener = { updates -> updates.map(::LobbyNotificationMessage).forEach(::send) },
                    )

                WsMessageHandler { wsMessage ->
                    when (val message = messageFromClient(wsMessage)) {
                        is StartGameMessage -> startGameController(playerSession)
                        is PlaceABidMessage -> placeABidController(playerSession, message.bid)
                        is PlayACardMessage -> playACardController(playerSession, message)
                    }
                }
            }
        }

    private val http4kServer = wsRouter.asServer(Undertow(port))

    fun start() = http4kServer.start()

    companion object {
        private fun createApp(): SkullKingApplication {
            val playerIdStorage = PlayerIdStorageInMemoryAdapter()

            return SkullKingApplication.constructFromPorts(
                lobbyEventStore =
                    EventStoreEsdbAdapter(
                        // TODO: I don't like that I need to provide this configuration in the server and the tests. seems ripe for
                        //  making a mistake.
                        streamNameProvider =
                            StreamNameProvider(
                                prefix = "lobby-events",
                                idToString = LobbyId::show,
                            ),
                        converter = JLobbyEvent,
                    ),
                lobbyNotifier = LobbyNotifierInMemoryAdapter(),
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
    val lobbyId: LobbyId,
    val playerId: PlayerId,
)

internal fun interface EstablishesAPlayerSession {
    fun establishPlayerSession(
        req: Request,
        ws: WsSession,
        lobbyNotificationListener: LobbyNotificationListener,
    ): PlayerSession
}
