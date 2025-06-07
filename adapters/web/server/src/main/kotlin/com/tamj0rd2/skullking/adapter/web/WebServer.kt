package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.esdb.EventStoreEsdbAdapter
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyNotificationMessage
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.application.SkullKingApplication.OutputPorts
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.failureOrNull
import java.net.ServerSocket
import org.http4k.core.Request
import org.http4k.lens.Header
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.WsHandler
import org.http4k.websocket.WsResponse
import org.slf4j.LoggerFactory

class WebServer(outputPorts: OutputPorts = createOutputPorts(), port: Int = getUnusedPort()) {
    private val application = SkullKingApplication.constructFromPorts(outputPorts)
    private val createLobbyController = CreateLobbyController(application)
    private val joinALobbyController = JoinALobbyController(application)
    private val startGameController = StartGameController(application)
    private val placeABidController = PlaceABidController(application)
    private val playACardController = PlayACardController(application)

    private val wsRouter =
        websockets(
            "/game" bind createLobbyController.asWsHandler(),
            "/game/{lobbyId}" bind joinALobbyController.asWsHandler(),
        )

    private fun EstablishesAPlayerSession.asWsHandler(): WsHandler = { req: Request ->
        val playerId = playerIdLens.extract(req)

        WsResponse {
            val ws = PerPlayerWebsocket(ws = it, playerId = playerId)

            val lobbyId =
                establishPlayerSession(
                    req = req,
                    sendAMessage = ws,
                    playerId = playerId,
                    lobbyNotificationListener = { updates ->
                        updates.map(::LobbyNotificationMessage).forEach(ws::send)
                    },
                )

            ws.onMessageReceived { message ->
                val error = handleMessageFromClient(playerId, lobbyId, message).failureOrNull()
                if (error != null) ws.send(ErrorMessage(error))
            }
        }
    }

    private fun handleMessageFromClient(
        playerId: PlayerId,
        lobbyId: LobbyId,
        message: MessageFromClient,
    ) =
        when (message) {
            is StartGameMessage -> startGameController.receive(playerId, lobbyId, message)
            is PlaceABidMessage -> placeABidController.receive(playerId, lobbyId, message)
            is PlayACardMessage -> playACardController.receive(playerId, lobbyId, message)
        }

    private val http4kServer = wsRouter.asServer(Undertow(port))

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    fun start() =
        http4kServer.start().also { logger.info("🚀 server started on localhost:${it.port()}") }

    companion object {
        private fun createOutputPorts(): OutputPorts =
            OutputPorts(lobbyEventStore = EventStoreEsdbAdapter.forLobbyEvents())

        private tailrec fun getUnusedPort(): Int =
            ServerSocket(0).use {
                val port = it.localPort
                if (port == Main.DEFAULT_PORT) return getUnusedPort()
                return port
            }

        private val playerIdLens =
            Header.map(nextIn = { PlayerId.parse(it) }, nextOut = { PlayerId.show(it) })
                .required("player_id")
    }
}

internal fun interface EstablishesAPlayerSession {
    fun establishPlayerSession(
        req: Request,
        sendAMessage: SendAMessage,
        playerId: PlayerId,
        lobbyNotificationListener: LobbyNotificationListener,
    ): LobbyId
}

interface SendAMessage {
    fun send(message: MessageToClient)

    operator fun invoke(message: MessageToClient) = send(message)
}

internal interface MessageReceiver<M : MessageFromClient> {
    fun receive(playerId: PlayerId, lobbyId: LobbyId, message: M): Result4k<*, LobbyErrorCode>
}
