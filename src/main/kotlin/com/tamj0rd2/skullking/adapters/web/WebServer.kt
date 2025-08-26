package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.application.OutputPorts
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Undertow
import org.http4k.server.asServer

class WebServer(outputPorts: OutputPorts, port: Int) : AutoCloseable {
    private val router = { req: Request -> Response(Status.OK) }
    private val server = router.asServer(Undertow(port))

    fun start() {
        server.start()
    }

    override fun close() {
        server.close()
    }
}
