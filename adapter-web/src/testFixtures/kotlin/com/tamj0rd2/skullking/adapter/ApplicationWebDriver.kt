package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameOutput
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameOutput
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.values.ZERO
import org.http4k.client.WebsocketClient
import org.http4k.core.Uri
import org.http4k.websocket.WsClient
import java.time.Duration

class ApplicationWebDriver(
    private val baseUri: Uri,
) : ApplicationDriver {
    private val ws by lazy { WebsocketClient.blocking(uri = baseUri, timeout = Duration.ofSeconds(5)) }
    private var playerId = PlayerId.ZERO

    override fun invoke(command: CreateNewGameCommand): CreateNewGameOutput {
        ws.send(wsLens(CreateNewGameMessage))
        val response = ws.responses().firstOfKind<GameCreatedMessage>()
        return CreateNewGameOutput(response.gameId)
    }

    override fun invoke(command: JoinGameCommand): JoinGameOutput {
        ws.send(wsLens(JoinGameMessage(command.gameId)))
        val response = ws.responses().firstOfKind<JoinAcknowledgedMessage>()
        playerId = response.playerId
        return JoinGameOutput(playerId)
    }

    private fun WsClient.responses() =
        received()
            .map(wsLens)
            .onEach { println("client: received $it") }
}

private inline fun <reified T : Message> Sequence<Message>.firstOfKind(): T = filterIsInstance<T>().first()
