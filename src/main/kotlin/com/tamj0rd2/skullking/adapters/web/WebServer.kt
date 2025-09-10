package com.tamj0rd2.skullking.adapters.web

import com.tamj0rd2.skullking.adapters.web.PartialBlock.Companion.partial
import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.PlaceBidInput
import com.tamj0rd2.skullking.application.ports.input.StartGameInput
import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.application.ports.output.OutputPorts
import com.tamj0rd2.skullking.application.services.using
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.JSealed
import com.ubertob.kondor.json.JStringRepresentable
import com.ubertob.kondor.json.ObjectNodeConverter
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.str
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

                        val html = viewGameHtml(joinGameUri = Uri.of("/games/new").query("playerId", playerId.value))

                        Response(Status.OK).contentType(ContentType.TEXT_HTML).body(html)
                    },
            )

    val createGameWsHandler =
        "/games/new" bindWs
            { req: Request ->
                val playerId = checkNotNull(req.query("playerId")).let(PlayerId::parse)

                WsResponse { ws: Websocket ->
                    var handler: PlayerWsHandler? = null
                    useCases.createGameUseCase.execute(
                        CreateGameInput(
                            receiveGameNotification = {
                                if (handler == null) handler = PlayerWsHandler(useCases, playerId, ws, it.gameId)
                                handler.receive(it)
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

                        val html = viewGameHtml(joinGameUri = Uri.of("/games/${GameId.show(gameId)}").query("playerId", playerId.value))

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
                            receiveGameNotification = PlayerWsHandler(useCases, playerId, ws, gameId),
                            playerId = playerId,
                        )
                    )
                }
            }

    private val staticFiles = static(ResourceLoader.Classpath("static"))

    private val httpRouter =
        routes(joinGameHttpHandler, createGameHttpHandler, gamesHttpHandler, staticFiles).withFilter(httpExceptionFilter)

    private val server = poly(createGameWsHandler, joinGameWsHandler, httpRouter).asServer(Undertow(port))

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

private class PlayerWsHandler(
    private val useCases: UseCases,
    private val playerId: PlayerId,
    private val ws: Websocket,
    private val gameId: GameId,
) : ReceiveGameNotification {
    init {
        ws.onClose { println("WebSocket closed for player $playerId") }

        ws.onError { error -> println("WebSocket error for player $playerId: ${error.message}") }

        ws.onMessage {
            println("Received message from player $playerId: $it")
            when (val message = JIncomingHtmxMessage.fromJson(it.bodyString()).orThrow()) {
                is IncomingHtmxMessage.StartGame -> useCases.startGameUseCase.execute(StartGameInput(gameId))
                is IncomingHtmxMessage.PlaceBid -> useCases.placeBidUseCase.execute(PlaceBidInput(gameId, playerId, message.bid))
            }
        }
    }

    override fun receive(state: PlayerSpecificGameState) {
        println("Notifying $playerId with state $state")
        ws.send(WsMessage(partial { partialGameState(state) }))
    }
}

sealed class IncomingHtmxMessage {
    data object StartGame : IncomingHtmxMessage()

    data class PlaceBid(val bid: Bid) : IncomingHtmxMessage()
}

private object JIncomingHtmxMessage : JSealed<IncomingHtmxMessage>() {
    override val discriminatorFieldName: String = "_action"

    override fun extractTypeName(obj: IncomingHtmxMessage): String =
        when (obj) {
            is IncomingHtmxMessage.StartGame -> "StartGame"
            is IncomingHtmxMessage.PlaceBid -> "PlaceBid"
        }

    override val subConverters: Map<String, ObjectNodeConverter<out IncomingHtmxMessage>> =
        mapOf("StartGame" to JStartGame, "PlaceBid" to JPlaceBid)
}

private object JStartGame : JAny<IncomingHtmxMessage.StartGame>() {
    override fun JsonNodeObject.deserializeOrThrow() = IncomingHtmxMessage.StartGame
}

private object JPlaceBid : JAny<IncomingHtmxMessage.PlaceBid>() {
    private val bid by str(JBid, IncomingHtmxMessage.PlaceBid::bid)

    override fun JsonNodeObject.deserializeOrThrow() = IncomingHtmxMessage.PlaceBid(bid = +bid)
}

private object JBid : JStringRepresentable<Bid>() {
    override val cons: (String) -> Bid = { it.toInt().let(Bid::fromInt) }
    override val render: (Bid) -> String = { it.toInt().toString() }
}
