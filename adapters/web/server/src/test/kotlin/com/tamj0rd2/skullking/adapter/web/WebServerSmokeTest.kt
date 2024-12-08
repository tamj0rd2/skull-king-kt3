package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.PlayerRole
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD

@SmokeTest
@Execution(SAME_THREAD)
class WebServerSmokeTest {
    @Test
    fun `can start a 2 player game`() {
        // TODO: this should test Main instead. Figure out how to do it.
        WebServer(port = Main.DEFAULT_PORT).start().use {
            val baseUri = Uri.of("ws://localhost:${it.port()}")

            val player1 = PlayerRole(SkullKingWebClient(baseUri))
            val player2 = PlayerRole(SkullKingWebClient(baseUri))

            val gameId = player1.createsAGame()
            player2.joinsAGame(gameId)
            player1.startsTheGame()

            // TODO: add a konsist test to make sure I'm at least using every game action once
        }
    }
}
