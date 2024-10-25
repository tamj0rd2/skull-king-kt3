package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.domain.model.auth.SessionId
import com.tamj0rd2.skullking.domain.model.game.GameId
import dev.forkhandles.values.random
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.meta
import org.http4k.core.ContentType
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Header

object CreateNewGameEndpoint {
    private val sessionIdLens =
        Header
            .map(
                nextIn = { SessionId.parse(it) },
                nextOut = { SessionId.show(it) },
            ).required("session_id")

    val contract =
        "/game" meta {
            summary = "Create game"
            description = "Creates a new game which players can join"
            produces += ContentType.APPLICATION_JSON
            preFlightExtraction = PreFlightExtraction.IgnoreBody
            headers += sessionIdLens

            val example = GameCreatedMessage(GameId.random())
            returning(Status.CREATED, httpLens to example)
        } bindContract POST

    fun newRequest(sessionId: SessionId) =
        contract
            .newRequest()
            .with(sessionIdLens of sessionId)

    val Request.sessionId: SessionId get() = sessionIdLens.extract(this)
}
