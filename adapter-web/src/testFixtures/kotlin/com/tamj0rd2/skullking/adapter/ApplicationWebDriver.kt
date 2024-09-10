package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateOutput
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import dev.forkhandles.values.ZERO
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.WsClient
import java.time.Duration

class ApplicationWebDriver(
    private val baseUri: Uri,
) : ApplicationDriver {
    private val ws by lazy { connectToWs() }
    private var playerId = PlayerId.ZERO

    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        ws.send(wsLens(JoinGameMessage(command.gameId)))
        val response = ws.responses().firstOfKind<JoinAcknowledgedMessage>()
        playerId = response.playerId
        return JoinGameOutput(playerId)
    }

    override fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput {
        ws.send(wsLens(GetGameStateMessage))
        return ws.responses().firstOfKind<GameStateMessage>().state
    }

    private fun connectToWs(): WsClient = WebsocketClient.blocking(uri = baseUri, timeout = Duration.ofSeconds(5))

    private fun WsClient.responses() =
        received()
            .map(wsLens)
            .onEach { println("client: received $it") }
}

private inline fun <reified T : Message> Sequence<Message>.firstOfKind(): T = filterIsInstance<T>().first()
