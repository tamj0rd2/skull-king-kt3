package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.adapters.web.PartialBlock.Companion.partial
import com.tamj0rd2.skullking.application.OutputPorts
import com.tamj0rd2.skullking.application.UseCases
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import org.http4k.core.ContentType
import org.http4k.core.Filter
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.body.form
import org.http4k.core.query
import org.http4k.lens.contentType
import org.http4k.lens.location
import org.http4k.routing.bindHttp
import org.http4k.routing.bindWs
import org.http4k.routing.path
import org.http4k.routing.poly
import org.http4k.routing.routes
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse

class WebServer(outputPorts: OutputPorts, port: Int) : AutoCloseable {
    private val useCases = UseCases.createFrom(outputPorts)

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
                        val playerId = checkNotNull(req.form("playerId")).let(PlayerId::parse)

                        useCases.createGameUseCase.execute(CreateGameInput(playerId))

                        Response(Status.SEE_OTHER).location(Uri.of("/games"))
                    },
            )

    private val gameHttpHandler =
        "/games/{gameId}" bindHttp
            routes(
                Method.POST to
                    { req: Request ->
                        val gameId = checkNotNull(req.path("gameId")).let(GameId::parse)
                        val playerId = checkNotNull(req.form("playerId")).let(PlayerId::parse)

                        val html =
                            viewGameHtml(
                                joinGameUri =
                                    Uri.of("/games/${GameId.show(gameId)}")
                                        .query("playerId", playerId.value)
                            )

                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    }
            )

    private val prototypeHandler =
        "/prototype" bindHttp
            routes(
                "/" bindHttp
                    { _: Request ->
                        val html = lobbyListHtml()
                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    },
                "create-game" bindHttp
                    { _: Request ->
                        val html = createGameHtml()
                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    },
            )

    val gameWsHandler =
        "/games/{gameId}" bindWs
            { req: Request ->
                val gameId = checkNotNull(req.path("gameId")).let(GameId::parse)
                val playerId = checkNotNull(req.query("playerId")).let(PlayerId::parse)

                WsResponse { ws: Websocket ->
                    useCases.joinGameUseCase.execute(
                        JoinGameInput(
                            gameId = gameId,
                            receiveGameNotification = { state ->
                                println("Notifying $playerId with state $state")
                                ws.send(WsMessage(partial { partialGameState(state) }))
                            },
                            playerId = playerId,
                        )
                    )
                }
            }

    private val httpRouter =
        routes(gameHttpHandler, gamesHttpHandler, prototypeHandler).withFilter(httpExceptionFilter)

    private val server = poly(gameWsHandler, httpRouter).asServer(Undertow(port))

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
