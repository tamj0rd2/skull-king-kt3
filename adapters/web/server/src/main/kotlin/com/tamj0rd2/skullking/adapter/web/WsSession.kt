package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.http4k.websocket.WsStatus

internal class WsSession(
    private val ws: Websocket,
    val sessionId: SessionId,
    val playerId: PlayerId,
    setup: WsSession.() -> WsMessageHandler,
) {
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

    fun send(message: MessageToClient) {
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
        println("server: $sessionId: $message")
    }

    companion object {
        fun asWsResponse(
            // TODO: remove this.
            sessionId: SessionId,
            playerId: PlayerId,
            setup: WsSession.() -> WsMessageHandler,
        ) = WsResponse { ws -> WsSession(ws, sessionId, playerId, setup) }
    }
}

fun interface WsMessageHandler {
    fun handle(message: WsMessage)
}
