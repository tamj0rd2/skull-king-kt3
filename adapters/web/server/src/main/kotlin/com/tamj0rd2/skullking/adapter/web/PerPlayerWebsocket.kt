package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.websocket.Websocket
import org.slf4j.LoggerFactory

class PerPlayerWebsocket(private val ws: Websocket, playerId: PlayerId) : SendAMessage {
    private val logger = LoggerFactory.getLogger("server: $playerId")

    override fun send(message: MessageToClient) {
        logger.info("sending: $message")
        ws.send(messageToClient(message))
    }

    fun error(errorMessage: ErrorMessage) {
        logger.warn("sending error message: $errorMessage", errorMessage.error)
        ws.send(messageToClient(errorMessage))
    }

    fun onMessageReceived(handler: MessageHandler) {
        ws.onMessage { wsMessage ->
            val message = messageFromClient(wsMessage)
            logger.info("received: $message")
            handler.handle(message)
        }
    }

    fun interface MessageHandler {
        fun handle(message: MessageFromClient)
    }
}
