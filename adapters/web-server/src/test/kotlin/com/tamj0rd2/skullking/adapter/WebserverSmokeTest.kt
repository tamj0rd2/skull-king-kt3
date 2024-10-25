package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.adapter.web.WebServer
import com.tamj0rd2.skullking.application.port.input.PlayerRole
import come.tamj0rd2.skullking.adapter.SkullKingWebClient
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

class WebserverSmokeTest {
    // TODO: this should test Main instead. Figure out how to do it.
    private val server = WebServer.start()
    private val baseUri = Uri.of("ws://localhost:${server.port()}")

    @Test
    fun `can start a 2 player game`() {
        val player1 = PlayerRole(SkullKingWebClient(baseUri))
        val player2 = PlayerRole(SkullKingWebClient(baseUri))

        val gameId = player1.createsAGame()
        player1.joinsAGame(gameId)
        player2.joinsAGame(gameId)
        player1.startsTheGame()

        // TODO: add a konsist test to make sure I'm at least using every game action once
    }
}
