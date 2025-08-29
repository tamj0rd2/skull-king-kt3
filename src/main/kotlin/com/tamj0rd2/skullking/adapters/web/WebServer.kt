package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.adapters.web.PartialBlock.Companion.partial
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.application.ports.output.OutputPorts
import com.tamj0rd2.skullking.application.services.using
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
import org.http4k.routing.ResourceLoader
import org.http4k.routing.bindHttp
import org.http4k.routing.bindWs
import org.http4k.routing.path
import org.http4k.routing.poly
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse

class WebServer(outputPorts: OutputPorts, port: Int) : AutoCloseable {
    private val useCases = UseCases.using(outputPorts)

    private val gamesHttpHandler =
        "/games" bindHttp
            routes(
                Method.GET to
                    { _: Request ->
                        val output = useCases.viewGamesUseCase.execute(ViewGamesInput)

                        val html = listGamesHtml(output.games)

                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    }
            )

    private val createGameHttpHandler =
        "/games/new" bindHttp
            routes(
                Method.GET to
                    { _: Request ->
                        val html = createGameHtml()
                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    },
                Method.POST to
                    { req: Request ->
                        val playerId = checkNotNull(req.form("playerId")).let(PlayerId::parse)

                        val html =
                            viewGameHtml(
                                joinGameUri = Uri.of("/games/new").query("playerId", playerId.value)
                            )

                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    },
            )

    val createGameWsHandler =
        "/games/new" bindWs
            { req: Request ->
                val playerId = checkNotNull(req.query("playerId")).let(PlayerId::parse)

                WsResponse { ws: Websocket ->
                    useCases.createGameUseCase.execute(
                        CreateGameInput(
                            receiveGameNotification = { state ->
                                println("Notifying $playerId with state $state")
                                ws.send(WsMessage(partial { partialGameState(state) }))
                            },
                            playerId = playerId,
                        )
                    )
                }
            }

    private val joinGameHttpHandler =
        "/games/{gameId}/join" bindHttp
            routes(
                Method.GET to
                    { req: Request ->
                        val gameId = checkNotNull(req.path("gameId")).let(GameId::parse)
                        val html = joinGameHtml(gameId)
                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    },
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
                    },
            )

    val joinGameWsHandler =
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

    private val staticFiles = static(ResourceLoader.Classpath("static"))

    private val httpRouter =
        routes(joinGameHttpHandler, createGameHttpHandler, gamesHttpHandler, staticFiles)
            .withFilter(httpExceptionFilter)

    private val server =
        poly(createGameWsHandler, joinGameWsHandler, httpRouter).asServer(Undertow(port))

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
    }
}
