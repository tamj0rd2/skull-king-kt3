package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus
import org.slf4j.LoggerFactory

interface SendAMessage {
    fun send(message: MessageToClient)

    operator fun invoke(message: MessageToClient) = send(message)
}

internal class WsSession(
    val ws: Websocket,
    val playerId: PlayerId,
    setup: WsSession.() -> WsMessageHandler,
) : SendAMessage {
    init {
        log("connected")
        ws.onError { log("error - $it") }
        ws.onClose { log("disconnecting - $it") }

        try {
            log("setting up session")
            onMessageReceived(setup())
        } catch (e: Exception) {
            log("failed to setup session")
            ws.close(WsStatus.REFUSE)
            throw e
        }

        log("session has been setup")
    }

    override fun send(message: MessageToClient) {
        log("sending: $message")
        ws.send(messageToClient(message))
    }

    private fun onMessageReceived(handler: WsMessageHandler) {
        ws.onMessage { message ->
            log("received: ${message.bodyString()}")
            handler.handle(message)
        }
    }

    private fun log(message: String) {
        logger.info("server: $playerId: $message")
    }

    companion object {
        fun asWsResponse(
            playerId: PlayerId,
            setup: WsSession.() -> WsMessageHandler,
        ) = WsResponse { ws -> WsSession(ws, playerId, setup) }

        private val logger = LoggerFactory.getLogger(WsSession::class.java.simpleName)
    }
}

fun interface WsMessageHandler {
    fun handle(message: WsMessage)
}
