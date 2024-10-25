package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.domain.model.auth.SessionId
import com.tamj0rd2.skullking.domain.model.game.GameId
import dev.forkhandles.values.random
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.meta
import org.http4k.core.ContentType
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Header

class CreateGameController(
    private val createNewGameUseCase: CreateNewGameUseCase,
) {
    val route = createGameRoute to ::handle

    private fun handle(request: Request): Response {
        val output = createNewGameUseCase(CreateNewGameCommand(request.sessionId))
        val message = GameCreatedMessage(output.gameId)
        return Response(Status.CREATED).with(httpLens of message)
    }

    companion object {
        val createGameRoute =
            "/game" meta {
                summary = "Create game"
                description = "Creates a new game which players can join"
                produces += ContentType.APPLICATION_JSON
                preFlightExtraction = PreFlightExtraction.IgnoreBody

                val example = GameCreatedMessage(GameId.random())
                returning(Status.CREATED, httpLens to example)
            } bindContract POST

        private val sessionIdLens =
            Header
                .map(
                    nextIn = { SessionId.parse(it) },
                    nextOut = { SessionId.show(it) },
                ).required("session_id")

        fun newRequest(sessionId: SessionId) = createGameRoute.newRequest().with(sessionIdLens of sessionId)

        val Request.sessionId: SessionId get() = sessionIdLens.extract(this)
    }
}
