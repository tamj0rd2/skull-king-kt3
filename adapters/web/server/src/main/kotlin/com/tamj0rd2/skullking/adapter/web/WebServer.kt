package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.esdb.EventStoreEsdbAdapter
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyNotificationMessage
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.application.SkullKingApplication.OutputPorts
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.core.Request
import org.http4k.lens.Header
import org.http4k.routing.websockets
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.WsHandler
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import org.http4k.routing.ws.bind as bindWs

class WebServer(
    outputPorts: OutputPorts = createOutputPorts(),
    port: Int = getUnusedPort(),
) {
    private val application = SkullKingApplication.constructFromPorts(outputPorts)
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
            val playerId = playerIdLens.extract(req)

            WsSession.asWsResponse(playerId) {
                val wsSession = this

                val playerSession =
                    establishPlayerSession(
                        req = req,
                        ws = wsSession,
                        lobbyNotificationListener = { updates -> updates.map(::LobbyNotificationMessage).forEach(::send) },
                    )

                WsMessageHandler { wsMessage ->
                    when (val message = messageFromClient(wsMessage)) {
                        is StartGameMessage ->
                            startGameController(
                                wsSession,
                                playerId,
                                playerSession.lobbyId,
                                message,
                            )
                        is PlaceABidMessage ->
                            placeABidController(
                                wsSession,
                                playerId,
                                playerSession.lobbyId,
                                message,
                            )
                        is PlayACardMessage ->
                            playACardController(
                                wsSession,
                                playerId,
                                playerSession.lobbyId,
                                message,
                            )
                    }
                }
            }
        }

    private val http4kServer = wsRouter.asServer(Undertow(port))

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    fun start() =
        http4kServer.start().also {
            logger.info("🚀 server started on localhost:${it.port()}")
        }

    companion object {
        private fun createOutputPorts(): OutputPorts =
            OutputPorts(
                lobbyEventStore = EventStoreEsdbAdapter.forLobbyEvents(),
            )

        private tailrec fun getUnusedPort(): Int =
            ServerSocket(0).use {
                val port = it.localPort
                if (port == Main.DEFAULT_PORT) return getUnusedPort()
                return port
            }

        private val playerIdLens =
            Header
                .map(
                    nextIn = { PlayerId.parse(it) },
                    nextOut = { PlayerId.show(it) },
                ).required("player_id")
    }
}

internal data class PlayerSession(
    val lobbyId: LobbyId,
)

internal fun interface EstablishesAPlayerSession {
    fun establishPlayerSession(
        req: Request,
        ws: WsSession,
        lobbyNotificationListener: LobbyNotificationListener,
    ): PlayerSession
}

internal interface MessageReceiver<M : MessageFromClient> {
    operator fun invoke(
        ws: MessageSender,
        playerId: PlayerId,
        lobbyId: LobbyId,
        message: M,
    )
}
