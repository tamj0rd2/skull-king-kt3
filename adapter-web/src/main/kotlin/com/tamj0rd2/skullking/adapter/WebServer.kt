package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.ApplicationDomainDriver
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase
import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.peekFailure
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.Path
import org.http4k.lens.asResult
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Http4kServer
import org.http4k.server.PolyHandler
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsResponse

object WebServer {
    @JvmStatic
    fun main(args: Array<String>) {
        createServer(
            application =
                ApplicationDomainDriver(
                    gameEventsPort = GameEventsDummyAdapter(),
                ),
        ).start()
    }

    fun createServer(application: ApplicationDomainDriver): Http4kServer {
        val gameIdLens =
            Path
                .map(nextIn = GameId.Companion::parse)
                .of("game-id")
                .asResult()

        val ws =
            websockets(
                "/join/{game-id}" bind { req ->
                    WsResponse { ws: Websocket ->
                        val gameId = gameIdLens(req).peekFailure { println(it) }.orThrow()
                        val playerId = application(JoinGameCommand(gameId)).playerId
                        ws.send(wsLens(JoinAcknowledgedMessage(playerId)))

                        ws.onMessage {
                            println("server: received $it")
                            ws.apply {
                                when (val message = wsLens(it)) {
                                    is GetGameStateMessage -> {
                                        val state =
                                            application(
                                                ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery(
                                                    gameId,
                                                    playerId,
                                                ),
                                            )
                                        send(wsLens(GameStateMessage(state)))
                                    }

                                    else -> println("server: unhandled message: $message")
                                }
                            }
                        }
                        ws.onError { println("server: error: $it") }
                        ws.onClose { println("server: client is disconnecting") }
                    }
                },
            )
        val http: HttpHandler = { _: Request -> Response(OK).body("hiya world") }

        return PolyHandler(http, ws).asServer(Undertow(9000))
    }
}

class GameEventsDummyAdapter : GameEventsPort {
    override fun findGameEvents(gameId: GameId): List<GameEvent> {
        TODO("Not yet implemented")
    }

    override fun saveGameEvents(events: List<GameEvent>) {
        TODO("Not yet implemented")
    }
}
