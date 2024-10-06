package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.adapter.SkullKingWebClient
import com.tamj0rd2.skullking.adapter.web.WebServer
import com.tamj0rd2.skullking.application.SkullKingApplication
import com.tamj0rd2.skullking.application.port.input.JoinGameGameUseCaseContract
import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault
import org.http4k.core.Uri
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Timeout

@Timeout(2)
class JoinGameWebAdapterTest : JoinGameGameUseCaseContract() {
    private val server = WebServer.createServer(SkullKingApplication.usingTestDoublesByDefault())

    override fun newPlayerRole(): PlayerRole = PlayerRole(SkullKingWebClient(Uri.of("ws://localhost:${server.port()}")))

    @BeforeEach
    fun startServer() {
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.close()
    }
}
