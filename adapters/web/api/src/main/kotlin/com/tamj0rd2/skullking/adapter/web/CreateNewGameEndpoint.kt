package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameId
import com.ubertob.kondor.json.JAny
import com.ubertob.kondor.json.jsonnode.JsonNodeObject
import com.ubertob.kondor.json.str
import dev.forkhandles.values.random
import org.http4k.asString
import org.http4k.contract.PreFlightExtraction
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.ContentType.Companion.APPLICATION_JSON
import org.http4k.core.Method.POST
import org.http4k.core.Status
import org.http4k.lens.ContentNegotiation
import org.http4k.lens.Header
import org.http4k.lens.httpBodyRoot

object CreateNewGameEndpoint {
    data class GameCreatedMessage(
        val gameId: GameId,
    )

    private object JGameCreatedMessage : JAny<GameCreatedMessage>() {
        private val gameId by str(JGameId, GameCreatedMessage::gameId)

        override fun JsonNodeObject.deserializeOrThrow() =
            GameCreatedMessage(
                gameId = +gameId,
            )
    }

    val gameCreatedMessageLens =
        httpBodyRoot(emptyList(), APPLICATION_JSON, ContentNegotiation.None)
            .map(
                nextIn = { JGameCreatedMessage.fromJson(it.payload.asString()).orThrow() },
                nextOut = { Body(JGameCreatedMessage.toJson(it)) },
            ).toLens()

    val sessionIdLens =
        Header
            .map(
                nextIn = { SessionId.parse(it) },
                nextOut = { SessionId.show(it) },
            ).required("session_id")

    val contract =
        "/game" meta {
            summary = "Create game"
            description = "Creates a new game which players can join"
            produces += APPLICATION_JSON
            preFlightExtraction = PreFlightExtraction.IgnoreBody
            headers += sessionIdLens

            val example = GameCreatedMessage(GameId.random())
            returning(Status.CREATED, gameCreatedMessageLens to example)
        } bindContract POST
}
