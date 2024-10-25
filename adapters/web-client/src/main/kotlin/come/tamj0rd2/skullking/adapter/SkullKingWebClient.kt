package come.tamj0rd2.skullking.adapter

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.adapter.CreateNewGameEndpoint
import com.tamj0rd2.skullking.adapter.ErrorMessage
import com.tamj0rd2.skullking.adapter.GameCreatedMessage
import com.tamj0rd2.skullking.adapter.GameUpdateMessage
import com.tamj0rd2.skullking.adapter.JoinAcknowledgedMessage
import com.tamj0rd2.skullking.adapter.Message
import com.tamj0rd2.skullking.adapter.StartGameMessage
import com.tamj0rd2.skullking.adapter.httpLens
import com.tamj0rd2.skullking.adapter.wsLens
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameOutput
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameOutput
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.auth.SessionId
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.GameStarted
import com.tamj0rd2.skullking.domain.model.game.PlayerId
import dev.forkhandles.result4k.Result4k
import org.http4k.client.ApacheClient
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters.SetBaseUriFrom
import org.http4k.websocket.Websocket
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.MILLISECONDS

class SkullKingWebClient(
    private val baseUri: Uri,
    private val timeoutMs: Long = 500,
) : SkullKingUseCases {
    private val httpClient = SetBaseUriFrom(baseUri.scheme("http")).then(ApacheClient())
    private lateinit var ws: Websocket
    private var playerId = PlayerId.NONE

    override fun invoke(command: CreateNewGameCommand): CreateNewGameOutput =
        httpClient(CreateNewGameEndpoint.newRequest(command.sessionId)).use {
            if (!it.status.successful) TODO("handle failure to create a new game")
            val response = httpLens(it) as GameCreatedMessage
            CreateNewGameOutput(response.gameId)
        }

    override fun invoke(command: JoinGameCommand): Result4k<JoinGameOutput, GameErrorCode> {
        ws = connectToWs(command.sessionId, command.gameId, command.gameUpdateListener)
        playerId = ws.waitForMessage<JoinAcknowledgedMessage>().playerId
        check(playerId != PlayerId.NONE) { "player id is zero still" }
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

        latch.await(timeoutMs, MILLISECONDS)
        failureReason?.let { throw it }
        return wantedMessage!!
    }

    private fun connectToWs(
        sessionId: SessionId,
        gameId: GameId,
        gameUpdateListener: GameUpdateListener,
    ): Websocket {
        val ws =
            WebsocketClient.nonBlocking(
                uri = baseUri.scheme("ws").path("/game/${GameId.show(gameId)}"),
                headers = listOf("session_id" to SessionId.show(sessionId)),
                timeout = Duration.ofSeconds(1),
                onConnect = { ws ->
                    println("client: $sessionId: connected")
                    ws.onMessage {
                        val message = wsLens(it)
                        println("client: $sessionId: received: $message")

                        if (message is GameUpdateMessage) {
                            gameUpdateListener.send(message.gameUpdate)
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
