package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.adapter.web.WebServer
import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

@Timeout(5)
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

        // TODO: continue from here when I implement starting the game
    }
}
