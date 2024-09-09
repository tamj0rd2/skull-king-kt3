package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import dev.forkhandles.values.ZERO
import org.http4k.core.Uri
import org.http4k.websocket.WsMessage
import java.time.Duration
import java.util.concurrent.CountDownLatch

class ApplicationWebDriver(
    private val baseUri: Uri,
) : ApplicationDriver {
    private val ws by lazy { connectToWs() }
    private var playerId = PlayerId.ZERO

    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        val response = ws.sendAndAwaitNextResponse(wsLens(JoinGameMessage(command.gameId))).asMessage<JoinAcknowledgedMessage>()
        playerId = response.playerId
        return JoinGameOutput(playerId)
    }

    override fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput {
        val response = ws.sendAndAwaitNextResponse(wsLens(GetGameStateMessage))
        return (wsLens(response) as GameStateMessage).state
    }

    private fun connectToWs(): SuperSocket {
        val countDownLatch = CountDownLatch(1)
        val ws =
            SuperSocket.nonBlocking(
                uri = baseUri,
                timeout = Duration.ofSeconds(5),
                onConnect = { countDownLatch.countDown() },
            )
        ws.onError { println("client: error: $it") }
        ws.onClose { println("client: connection closed: $it") }
        countDownLatch.await()
        println("client: connected")
        return ws
    }
}

private fun <T : Message> WsMessage.asMessage(): T {
    @Suppress("UNCHECKED_CAST")
    return wsLens(this) as T
}
