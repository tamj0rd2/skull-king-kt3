package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import dev.forkhandles.values.random
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Timeout(5)
class WebserverSmokeTest {
    private val server = WebServer.start()
    private val baseUri = Uri.of("ws://localhost:${server.port()}")

    @Test
    fun `can start a 2 player game`() {
        val player1 = ApplicationWebDriver(baseUri)
        val player2 = ApplicationWebDriver(baseUri)

        val gameId = GameId.random()
        player1(JoinGameCommand(gameId)).playerId
        player2(JoinGameCommand(gameId)).playerId

        // TODO: continue from here when I implement starting the game
    }
}
