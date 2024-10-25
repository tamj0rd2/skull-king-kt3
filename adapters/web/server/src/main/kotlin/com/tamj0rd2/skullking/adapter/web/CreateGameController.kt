package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.CreateNewGameEndpoint.sessionId
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

class CreateGameController(
    private val createNewGameUseCase: CreateNewGameUseCase,
) {
    val contractRoute = CreateNewGameEndpoint.contract to ::handle

    private fun handle(request: Request): Response {
        val output = createNewGameUseCase(CreateNewGameCommand(request.sessionId))
        val message = GameCreatedMessage(output.gameId)
        return Response(Status.CREATED).with(httpLens of message)
    }
}
