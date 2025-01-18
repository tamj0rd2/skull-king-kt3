package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlayACardMessage
import com.tamj0rd2.skullking.adapter.web.MessageFromClient.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.GameCreatedMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.GameUpdateMessage
import com.tamj0rd2.skullking.adapter.web.MessageToClient.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.BidPlaced
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.CardPlayed
import com.tamj0rd2.skullking.application.port.inandout.GameUpdate.GameStarted
import com.tamj0rd2.skullking.application.port.inandout.GameUpdateListener
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameOutput
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidOutput
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardCommand
import com.tamj0rd2.skullking.application.port.input.PlayACardUseCase.PlayACardOutput
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameErrorCode
import com.tamj0rd2.skullking.domain.game.GameId
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

    override fun invoke(command: CreateNewGameCommand): CreateNewGameOutput {
        ws =
            connectToWs(
                path = "/game",
                sessionId = command.sessionId,
                gameUpdateListener = command.gameUpdateListener,
            )

        val message = ws.waitForMessage<GameCreatedMessage>()
        check(message.playerId != PlayerId.NONE) { "got a zero playerId" }
        check(message.gameId != GameId.NONE) { "got a zero gameId" }

        return CreateNewGameOutput(
            gameId = message.gameId,
            playerId = message.playerId,
        )
    }

    override fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode> {
        ws =
            connectToWs(
                path = "/game/${GameId.show(command.gameId)}",
                sessionId = command.sessionId,
                gameUpdateListener = command.gameUpdateListener,
            )

        val message = ws.waitForMessage<JoinAcknowledgedMessage>()
        check(message.playerId != PlayerId.NONE) { "got a zero playerId" }

        return JoinGameOutput(playerId = message.playerId).asSuccess()
    }

    override fun invoke(command: StartGameCommand): Result4k<StartGameOutput, GameErrorCode> {
        ws.send(messageFromClient(StartGameMessage))
        ws.waitForGameUpdate<GameStarted>()
        return StartGameOutput.asSuccess()
    }

    override fun invoke(command: PlayACardCommand): Result4k<PlayACardOutput, GameErrorCode> {
        ws.send(messageFromClient(PlayACardMessage(command.card)))
        ws.waitForGameUpdate<CardPlayed>()
        return PlayACardOutput.asSuccess()
    }

    override fun invoke(command: PlaceABidCommand): Result4k<PlaceABidOutput, GameErrorCode> {
        ws.send(messageFromClient(PlaceABidMessage(command.bid)))
        ws.waitForGameUpdate<BidPlaced>()
        return PlaceABidOutput.asSuccess()
    }

    private inline fun <reified T : GameUpdate> Websocket.waitForGameUpdate(): T =
        waitForMessage<GameUpdateMessage> { it.gameUpdate is T }.gameUpdate as T

    private inline fun <reified T : MessageToClient> Websocket.waitForMessage(crossinline matcher: (T) -> Boolean = { true }): T {
        val latch = CountDownLatch(1)
        var failureReason: GameErrorCode? = null
        var wantedMessage: T? = null
        val allSeenMessages = mutableListOf<MessageToClient>()

        onMessage {
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
        gameUpdateListener: GameUpdateListener,
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

                        if (message is GameUpdateMessage) {
                            gameUpdateListener.receive(message.gameUpdate)
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
