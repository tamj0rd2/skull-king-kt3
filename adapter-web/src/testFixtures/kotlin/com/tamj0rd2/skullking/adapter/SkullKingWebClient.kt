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
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.GameStarted
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

    override fun invoke(command: CreateNewGameCommand): CreateNewGameOutput =
        httpClient(CreateGameController.newRequest()).use {
            expectThat(it).status.isEqualTo(Status.CREATED)
            val response = httpLens(it) as GameCreatedMessage
            CreateNewGameOutput(response.gameId)
        }

    override fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode> {
        ws = connectToWs(command.gameId, command.gameUpdateListener)
        playerId = ws.waitForMessage<JoinAcknowledgedMessage>().playerId
        check(playerId != PlayerId.ZERO) { "player id is zero still" }
        return JoinGameOutput(playerId).asSuccess()
    }

    override fun invoke(command: StartGameCommand): Result4k<StartGameOutput, GameErrorCode> {
        ws.send(wsLens(StartGameMessage))
        ws.waitForGameUpdate<GameStarted>()
        return StartGameOutput.asSuccess()
    }

    private inline fun <reified T : GameUpdate> Websocket.waitForGameUpdate(): T =
        waitForMessage<GameUpdateMessage> { it.gameUpdate is T }.gameUpdate as T

    private inline fun <reified T : Message> Websocket.waitForMessage(crossinline matcher: (T) -> Boolean = { true }): T {
        val latch = CountDownLatch(1)
        var failureReason: GameErrorCode? = null
        var wantedMessage: T? = null

        onMessage {
            val message = wsLens(it)

            if (message is T && matcher(message)) {
                wantedMessage = message
                latch.countDown()
            }

            if (message is ErrorMessage) {
                failureReason = message.error
                latch.countDown()
            }
        }

        latch.await()
        failureReason?.let { throw it }
        return wantedMessage!!
    }

    private fun connectToWs(
        gameId: GameId,
        gameUpdateListener: GameUpdateListener,
    ): Websocket {
        val clientId = newClientId()
        val ws =
            WebsocketClient.nonBlocking(
                uri = baseUri.scheme("ws").path("/game/${GameId.show(gameId)}"),
                timeout = Duration.ofSeconds(1),
                onConnect = { ws ->
                    println("client $clientId connected")
                    ws.onMessage {
                        val message = wsLens(it)
                        println("client $clientId received: $message")

                        if (message is GameUpdateMessage) {
                            gameUpdateListener.send(message.gameUpdate)
                        }
                    }
                },
                onError = { println("client $clientId error: $it") },
            )
        ws.onClose { println("client $clientId closed: $it") }
        ws.onError { println("client $clientId error: $it") }
        return ws
    }

    companion object {
        private var clientCount = 0

        private fun newClientId(): Int = ++clientCount
    }
}
