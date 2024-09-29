package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.adapter.ApplicationWebDriver
import com.tamj0rd2.skullking.adapter.web.WebServer
import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.input.StartGameUseCaseContract
import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault
import org.http4k.core.Uri
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Timeout

@Timeout(2)
class StartGameWebAdapterTest : StartGameUseCaseContract() {
    private val server = WebServer.createServer(ApplicationDomainDriver.usingTestDoublesByDefault())

    override fun newPlayerRole(): PlayerRole = PlayerRole(ApplicationWebDriver(Uri.of("ws://localhost:${server.port()}")))

    @BeforeEach
    fun startServer() {
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.close()
    }
}
