package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.adapter.web.CreateGameController
import com.tamj0rd2.skullking.adapter.web.ErrorMessage
import com.tamj0rd2.skullking.adapter.web.GameCreatedMessage
import com.tamj0rd2.skullking.adapter.web.GameUpdateMessage
import com.tamj0rd2.skullking.adapter.web.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.adapter.web.Message
import com.tamj0rd2.skullking.adapter.web.StartGameMessage
import com.tamj0rd2.skullking.adapter.web.httpLens
import com.tamj0rd2.skullking.adapter.web.wsLens
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameOutput
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.values.ZERO
import org.http4k.client.ApacheClient
import org.http4k.client.WebsocketClient
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.strikt.status
import org.http4k.websocket.Websocket
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Duration
import java.util.concurrent.CountDownLatch

class SkullKingWebClient(
    private val baseUri: Uri,
) : SkullKingUseCases {
    private val httpClient = SetBaseUriFrom(baseUri.scheme("http")).then(ApacheClient())
    private lateinit var ws: Websocket
    private var playerId = PlayerId.ZERO
    private lateinit var gameUpdateListener: GameUpdateListener
    private val allReceivedMessages = mutableListOf<Message>()

    override fun invoke(command: CreateNewGameCommand): CreateNewGameOutput =
        httpClient(CreateGameController.newRequest()).use {
            expectThat(it).status.isEqualTo(Status.CREATED)
            val response = httpLens(it) as GameCreatedMessage
            CreateNewGameOutput(response.gameId)
        }

    override fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode> {
        gameUpdateListener = command.gameUpdateListener
        ws = connectToWs(command.gameId)
        ws.waitForJoinAcknowledgement()
        ws.onMessage {
            val message = wsLens(it)
            if (message is GameUpdateMessage) {
                command.gameUpdateListener.send(message.gameUpdate)
            }
        }

        check(playerId != PlayerId.ZERO) { "the player id has not been set" }
        return JoinGameOutput(playerId).asSuccess()
    }

    override fun invoke(command: StartGameCommand): StartGameOutput {
        ws.send(wsLens(StartGameMessage))
        return StartGameOutput
    }

    private fun Websocket.waitForJoinAcknowledgement() {
        val latch = CountDownLatch(1)
        var failureReason: GameErrorCode? = null

        onMessage {
            if (playerId != PlayerId.ZERO) return@onMessage

            when (val message = wsLens(it)) {
                is JoinAcknowledgedMessage -> {
                    playerId = message.playerId
                    latch.countDown()
                }
                is ErrorMessage -> {
                    failureReason = message.error
                    latch.countDown()
                }
                else -> Unit
            }
        }

        latch.await()
        failureReason?.let { throw it }
        check(playerId != PlayerId.ZERO) { "the player id has not been set" }
    }

    private fun connectToWs(gameId: GameId): Websocket {
        val clientId = newClientId()
        val ws =
            WebsocketClient.nonBlocking(
                uri = baseUri.scheme("ws").path("/game/${GameId.show(gameId)}"),
                timeout = Duration.ofSeconds(1),
                onConnect = { println("client $clientId connected") },
            )
        ws.onClose { println("client $clientId closed: $it") }
        ws.onError { println("client $clientId error: $it") }
        ws.onMessage {
            val message = wsLens(it)
            allReceivedMessages.add(message)
            println("client $clientId received: $message")
        }
        return ws
    }

    companion object {
        private var clientCount = 0

        private fun newClientId(): Int = ++clientCount
    }
}
