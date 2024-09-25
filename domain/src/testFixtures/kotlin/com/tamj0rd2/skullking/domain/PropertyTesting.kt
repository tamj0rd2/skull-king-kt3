package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.GameCreated
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import strikt.api.Assertion
import strikt.assertions.isA

object GameArbs {
    val gameIdArb = Arb.uuid(allowNilValue = false).map { GameId.of(it) }
    val playerIdArb = Arb.uuid().map { PlayerId.of(it) }

    fun gameCreatedArb(gameId: GameId) = arbitrary { GameCreated(gameId) }

    fun playerJoinedArb(gameId: GameId) = playerIdArb.map { playerId -> PlayerJoined(gameId, playerId) }

    val gameEventArb =
        gameIdArb.flatMap { gameId ->
            Arb.choice(
                gameCreatedArb(gameId),
                playerJoinedArb(gameId),
            )
        }

    // TODO: modify this as I create more events.
    val gameEventsArb = Arb.list(gameEventArb)

    val validGameEventsArb =
        arbitrary {
            val gameId = gameIdArb.bind()

            buildList {
                add(gameCreatedArb(gameId).bind())
                addAll(Arb.set(playerJoinedArb(gameId), 0..MAXIMUM_PLAYER_COUNT).bind())
            }
        }

    val gameArb =
        validGameEventsArb
            .map { history -> runCatching { Game.from(history) }.getOrNull() }
            .filterNot { it == null }
            .map { requireNotNull(it) }
}

fun propertyTest(block: suspend () -> Unit) = runBlocking(block)

fun <T, E> Assertion.Builder<Result4k<T, E>>.wasSuccessful() = run { isA<Success<*>>() }
