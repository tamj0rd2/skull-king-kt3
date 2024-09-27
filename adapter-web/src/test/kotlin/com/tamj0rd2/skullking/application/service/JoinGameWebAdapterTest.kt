package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.adapter.ApplicationWebDriver
import com.tamj0rd2.skullking.adapter.WebServer
import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.ApplicationDriver
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCaseContract
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault
import org.http4k.core.Uri
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Timeout

@Timeout(2)
class JoinGameWebAdapterTest : JoinGameUseCaseContract() {
    private val server = WebServer.createServer(ApplicationDomainDriver.usingTestDoublesByDefault())

    override fun newDriver(): ApplicationDriver = ApplicationWebDriver(Uri.of("ws://localhost:${server.port()}"))

    @BeforeEach
    fun startServer() {
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.close()
    }
}
