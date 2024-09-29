package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.adapter.web.*
import com.tamj0rd2.skullking.adapter.web.httpLens
import com.tamj0rd2.skullking.adapter.web.wsLens
import com.tamj0rd2.skullking.application.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameOutput
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
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

class ApplicationWebDriver(
    private val baseUri: Uri,
) : ApplicationDriver {
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

    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        gameUpdateListener = command.gameUpdateListener
        ws = connectToWs(command.gameId)
        ws.waitForJoinAcknowledgement()
        ws.onMessage {
            val message = wsLens(it)
            if (message is GameUpdateMessage) {
                println("received a game update message!")
                command.gameUpdateListener.send(message.gameUpdate)
            }
        }

        check(playerId != PlayerId.ZERO) { "the player id has not been set" }
        return JoinGameOutput(playerId)
    }

    private fun Websocket.waitForJoinAcknowledgement() {
        val latch = CountDownLatch(1)

        onMessage {
            if (playerId != PlayerId.ZERO) return@onMessage

            val message = wsLens(it)
            if (message is JoinAcknowledgedMessage) {
                playerId = message.playerId
                latch.countDown()
            }
        }

        latch.await()
    }

    private fun connectToWs(gameId: GameId): Websocket {
        val ws =
            WebsocketClient.nonBlocking(
                uri = baseUri.scheme("ws").path("/game/${GameId.show(gameId)}"),
                timeout = Duration.ofSeconds(1),
                onConnect = { println("client connected") },
            )
        ws.onClose { println("client closed: $it") }
        ws.onError { println("client error: $it") }
        ws.onMessage {
            val message = wsLens(it)
            allReceivedMessages.add(message)
            println("client received: $message")
        }
        return ws
    }
}
