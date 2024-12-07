package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.serialization.json.JGameId
import com.tamj0rd2.skullking.serialization.json.JPlayerId
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
    // TODO: move this to live with the other ws messages
    data class GameCreatedMessage(
        val gameId: GameId,
        val playerId: PlayerId,
    ) : MessageToClient

    // TODO: move this to MessageToClient.kt
    internal object JGameCreatedMessage : JAny<GameCreatedMessage>() {
        private val gameId by str(JGameId, GameCreatedMessage::gameId)
        private val createdBy by str(JPlayerId, GameCreatedMessage::playerId)

        override fun JsonNodeObject.deserializeOrThrow() =
            GameCreatedMessage(
                gameId = +gameId,
                playerId = +createdBy,
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

            val example = GameCreatedMessage(gameId = GameId.random(), playerId = PlayerId.random())
            returning(Status.CREATED, gameCreatedMessageLens to example)
        } bindContract POST
}
