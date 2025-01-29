package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyCreatedMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.LobbyNotificationMessage
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewLobbyUseCase.CreateNewLobbyOutput
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyCommand
import com.tamj0rd2.skullking.application.port.input.JoinALobbyUseCase.JoinALobbyOutput
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidOutput
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardOutput
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ABidWasPlaced
import com.tamj0rd2.skullking.domain.game.LobbyNotification.ACardWasPlayed
import com.tamj0rd2.skullking.domain.game.LobbyNotification.TheGameHasStarted
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.Websocket
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS

class SkullKingWebClient(
    private val baseUri: Uri,
    private val timeoutMs: Long = 500,
) : SkullKingUseCases,
    AutoCloseable {
    private lateinit var ws: Websocket

    override fun close() {
        if (this::ws.isInitialized) ws.close()
    }

    override fun invoke(command: CreateNewLobbyCommand): CreateNewLobbyOutput {
        ws =
            connectToWs(
                path = "/game",
                sessionId = command.sessionId,
                lobbyNotificationListener = command.lobbyNotificationListener,
            )

        val message = ws.waitForMessage<LobbyCreatedMessage>()
        check(message.playerId != PlayerId.NONE) { "got a zero playerId" }
        check(message.lobbyId != LobbyId.NONE) { "got a zero lobbyId" }

        return CreateNewLobbyOutput(
            lobbyId = message.lobbyId,
            playerId = message.playerId,
        )
    }

    override fun invoke(command: JoinALobbyCommand): Result4k<JoinALobbyOutput, LobbyErrorCode> {
        ws =
            connectToWs(
                path = "/game/${LobbyId.show(command.lobbyId)}",
                sessionId = command.sessionId,
                lobbyNotificationListener = command.lobbyNotificationListener,
            )

        val message = ws.waitForMessage<JoinAcknowledgedMessage>()
        check(message.playerId != PlayerId.NONE) { "got a zero playerId" }

        return JoinALobbyOutput(playerId = message.playerId).asSuccess()
    }

    override fun invoke(command: StartGameCommand): Result4k<StartGameOutput, LobbyErrorCode> {
        ws.send(messageFromClient(StartGameMessage))
        ws.waitForLobbyNotification<TheGameHasStarted>()
        return StartGameOutput.asSuccess()
    }

    override fun invoke(command: PlayACardCommand): Result4k<PlayACardOutput, LobbyErrorCode> {
        ws.send(messageFromClient(PlayACardMessage(command.card)))
        ws.waitForLobbyNotification<ACardWasPlayed>()
        return PlayACardOutput.asSuccess()
    }

    override fun invoke(command: PlaceABidCommand): Result4k<PlaceABidOutput, LobbyErrorCode> {
        ws.send(messageFromClient(PlaceABidMessage(command.bid)))
        ws.waitForLobbyNotification<ABidWasPlaced>()
        return PlaceABidOutput.asSuccess()
    }

    private inline fun <reified T : LobbyNotification> Websocket.waitForLobbyNotification(): T =
        waitForMessage<LobbyNotificationMessage> { it.lobbyNotification is T }.lobbyNotification as T

    private inline fun <reified T : MessageToClient> Websocket.waitForMessage(crossinline matcher: (T) -> Boolean = { true }): T {
        val latch = CountDownLatch(1)
        var failureReason: LobbyErrorCode? = null
        var wantedMessage: T? = null
        val allSeenMessages = mutableListOf<MessageToClient>()

        onMessage {
            if (wantedMessage != null) return@onMessage

            val message = messageToClient(it)
            allSeenMessages.add(message)

            if (message is T && matcher(message)) {
                wantedMessage = message
                latch.countDown()
            }

            if (message is ErrorMessage) {
                failureReason = message.error
                latch.countDown()
            }
        }

        latch.await(timeoutMs, MILLISECONDS)
        failureReason?.let { throw it }

        check(allSeenMessages.isNotEmpty()) { "did not receive any messages within the given timeout." }
        checkNotNull(wantedMessage) {
            "did not receive the ${T::class.java.simpleName} message within the given timeout. Messages:\n${allSeenMessages.joinToString(
                "\n- ",
            )}"
        }
        return wantedMessage as T
    }

    private fun connectToWs(
        path: String,
        sessionId: SessionId,
        lobbyNotificationListener: LobbyNotificationListener,
    ): Websocket {
        val ws =
            WebsocketClient.nonBlocking(
                uri = baseUri.scheme("ws").path(path),
                headers = listOf("session_id" to SessionId.show(sessionId)),
                timeout = Duration.ofSeconds(1),
                onConnect = { ws ->
                    println("client: $sessionId: connected")
                    ws.onMessage {
                        val message = messageToClient(it)
                        println("client: $sessionId: received: $message")

                        if (message is LobbyNotificationMessage) {
                            lobbyNotificationListener.receive(message.lobbyNotification)
                        }
                    }
                },
                onError = { println("client: $sessionId: error: $it") },
            )
        ws.onClose { println("client: $sessionId: closed: $it") }
        ws.onError { println("client: $sessionId: error: $it") }
        return ws
    }
}
