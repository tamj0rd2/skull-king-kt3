package com.tamj0rd2.skullking.adapter

import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsConsumer
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsStatus
import org.http4k.websocket.WsStatus.Companion.NORMAL
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.time.Duration
import java.time.Duration.ZERO
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicReference

class SuperSocket(
    private val client: WebSocketClient,
) : Websocket {
    // todo: I think this will work nicely with a sequence, because I can tell it how many messages to take before closing.
    fun sendAndAwaitNextResponse(message: WsMessage): WsMessage {
        val latch = CountDownLatch(1)
        val handlerId = UUID.randomUUID()

        var receivedMessage: WsMessage? = null
        messageHandlers[handlerId] = {
            messageHandlers.remove(handlerId)
            receivedMessage = it
            latch.countDown()
        }

        send(message)
        latch.await(300, MILLISECONDS)
        return requireNotNull(receivedMessage) { "no followup messages were received by the client" }
    }

    override fun send(message: WsMessage) =
        when (message.mode) {
            WsMessage.Mode.Binary -> client.send(message.body.payload)
            WsMessage.Mode.Text -> client.send(message.bodyString())
        }

    override fun close(status: WsStatus) = client.close(status.code, status.description)

    private val errorHandlers: MutableList<(Throwable) -> Unit> = mutableListOf()
    private val closeHandlers: MutableList<(WsStatus) -> Unit> = mutableListOf()
    private val messageHandlers: MutableMap<UUID, (WsMessage) -> Unit> = mutableMapOf()

    fun triggerError(throwable: Throwable) = errorHandlers.forEach { it(throwable) }

    fun triggerClose(status: WsStatus = NORMAL) = closeHandlers.forEach { it(status) }

    fun triggerMessage(message: WsMessage) = messageHandlers.forEach { it.value(message) }

    override fun onError(fn: (Throwable) -> Unit) {
        errorHandlers.add(fn)
    }

    override fun onClose(fn: (WsStatus) -> Unit) {
        closeHandlers.add(fn)
    }

    override fun onMessage(fn: (WsMessage) -> Unit) {
        messageHandlers[UUID.randomUUID()] = fn
    }

    companion object {
        fun nonBlocking(
            uri: Uri,
            timeout: Duration = ZERO,
            onError: (Throwable) -> Unit = {},
            draft: Draft = Draft_6455(),
            onConnect: WsConsumer = {},
        ): SuperSocket {
            val socket = AtomicReference<SuperSocket>()
            val client = NonBlockingClient(uri, timeout, onConnect, draft, socket)
            socket.set(SuperSocket(client).apply { onError(onError) })
            client.connect()
            return socket.get()
        }
    }
}

private class NonBlockingClient(
    uri: Uri,
    timeout: Duration,
    private val onConnect: WsConsumer,
    draft: Draft,
    private val socket: AtomicReference<SuperSocket>,
) : WebSocketClient(URI.create(uri.toString()), draft, emptyMap(), timeout.toMillis().toInt()) {
    override fun onOpen(handshakedata: ServerHandshake?) {
        onConnect(socket.get())
    }

    override fun onClose(
        code: Int,
        reason: String,
        remote: Boolean,
    ) = socket.get().triggerClose(WsStatus(code, reason))

    override fun onMessage(message: String) = socket.get().triggerMessage(WsMessage(message))

    override fun onMessage(bytes: ByteBuffer) = socket.get().triggerMessage(WsMessage(bytes))

    override fun onError(e: Exception) = socket.get().triggerError(e)
}
