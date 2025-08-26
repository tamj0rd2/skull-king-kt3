package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.adapters.web.PartialBlock.Companion.partial
import com.tamj0rd2.skullking.application.OutputPorts
import com.tamj0rd2.skullking.application.UseCases
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.body.form
import org.http4k.lens.contentType
import org.http4k.lens.location
import org.http4k.routing.bindHttp
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer

class WebServer(outputPorts: OutputPorts, port: Int) : AutoCloseable {
    private val useCases =
        UseCases.createFrom(outputPorts)
            .monitorWith(
                inputMonitor = { println("Input: $it") },
                outputMonitor = { println("Output: $it") },
            )

    private val gamesHttpHandler =
        "/games" bindHttp
            routes(
                Method.GET to
                    { req: Request ->
                        val output = useCases.viewGamesUseCase.execute(ViewGamesInput)

                        val html =
                            if (req.htmx) partial { partialGamesHtml(output.games) }
                            else viewGamesHtml(output.games)

                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    },
                Method.POST to
                    { req: Request ->
                        val playerId = checkNotNull(req.form("player_id")).let(PlayerId::parse)

                        useCases.createGameUseCase.execute(CreateGameInput(playerId))

                        Response(Status.SEE_OTHER).location(Uri.of("/games"))
                    },
            )

    private val router = routes(gamesHttpHandler).withFilter(httpExceptionFilter)

    private val server = router.asServer(Undertow(port))

    fun start() {
        server.start()
    }

    override fun close() {
        server.close()
    }

    companion object {
        private val httpExceptionFilter = Filter { next ->
            { req ->
                try {
                    next(req)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Response(Status.OK).body("An error occurred")
                }
            }
        }

        private val Request.htmx: Boolean
            get() = header("HX-Request") == "true"
    }
}
