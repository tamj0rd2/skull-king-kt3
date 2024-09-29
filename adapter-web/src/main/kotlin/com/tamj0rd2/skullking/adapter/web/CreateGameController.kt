package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.domain.model.GameId
import dev.forkhandles.values.random
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.meta
import org.http4k.core.ContentType
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

class CreateGameController(
    private val createNewGameUseCase: CreateNewGameUseCase,
) {
    val route = createGameRoute to ::handle

    private fun handle(
        @Suppress("UNUSED_PARAMETER") request: Request,
    ): Response {
        val output = createNewGameUseCase(CreateNewGameCommand)
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

        fun newRequest() = createGameRoute.newRequest()
    }
}
