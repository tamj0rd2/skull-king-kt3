package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import dev.forkhandles.values.ZERO
import org.http4k.core.Uri
import java.time.Duration
import java.util.concurrent.CountDownLatch

class ApplicationWebDriver(
    private val baseUri: Uri,
) : ApplicationDriver {
    private lateinit var ws: SuperSocket
    private var playerId = PlayerId.ZERO

    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        ws = connectToWs(command.gameId)
        require(playerId != PlayerId.ZERO) { "playerId not set" }
        return JoinGameOutput(playerId)
    }

    override fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput {
        val response = ws.sendAndAwaitNextResponse(wsLens(GetGameStateMessage))
        return (wsLens(response) as GameStateMessage).state
    }

    private fun handleReceivedMessage(it: Message) {
        when (it) {
            is JoinAcknowledgedMessage -> error("this should only ever happen once during connection")
            else -> println("client: received message: $it")
        }
    }

    private fun connectToWs(gameId: GameId): SuperSocket {
        val countDownLatch = CountDownLatch(2)
        val ws =
            SuperSocket.nonBlocking(
                baseUri.path("/join/${gameId.externalForm()}"),
                timeout = Duration.ofSeconds(5),
            ) { countDownLatch.countDown() }

        ws.onError { println("client: error: $it") }
        ws.onClose { println("client: connection closed: $it") }
        ws.onMessage {
            when (val message = wsLens(it)) {
                is JoinAcknowledgedMessage -> {
                    playerId = message.playerId
                    countDownLatch.countDown()
                }

                else -> handleReceivedMessage(message)
            }
        }
        countDownLatch.await()
        return ws
    }
}
