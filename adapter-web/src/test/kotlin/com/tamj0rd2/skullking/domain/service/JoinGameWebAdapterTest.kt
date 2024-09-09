package com.tamj0rd2.skullking.domain.service

import com.tamj0rd2.skullking.ApplicationDomainDriver
import com.tamj0rd2.skullking.ApplicationDriver
import com.tamj0rd2.skullking.adapter.ApplicationWebDriver
import com.tamj0rd2.skullking.adapter.WebServer
import com.tamj0rd2.skullking.port.input.JoinGameUseCaseContract
import com.tamj0rd2.skullking.port.output.GameEventsInMemoryAdapter
import org.http4k.core.Uri
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Timeout

@Timeout(2)
class JoinGameWebAdapterTest : JoinGameUseCaseContract() {
    private val server =
        WebServer.createServer(
            ApplicationDomainDriver.create(
                gameEventsPort = GameEventsInMemoryAdapter(),
            ),
        )
    override val driver: ApplicationDriver = ApplicationWebDriver(Uri.of("ws://localhost:${server.port()}"))

    @BeforeEach
    fun startServer() {
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.close()
    }
}
