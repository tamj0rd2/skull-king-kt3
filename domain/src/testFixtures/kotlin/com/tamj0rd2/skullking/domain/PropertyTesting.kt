package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.GameCreated
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
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

    val validGameActionsArb =
        validGameEventsArb.map { events ->
            events.drop(1).map { event ->
                val description =
                    when (event) {
                        is GameCreated -> "Game created"
                        is PlayerJoined -> "Player ${PlayerId.show(event.playerId)} joined"
                    }

                GameAction(description) {
                    appendEvent(event.withGameId(id)).orThrow()
                    this
                }
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

data class GameAction(
    private val description: String,
    val mutate: Game.() -> Game,
) {
    override fun toString(): String = description
}

private fun GameEvent.withGameId(gameId: GameId) =
    when (this) {
        is GameCreated -> copy(gameId = gameId)
        is PlayerJoined -> copy(gameId = gameId)
    }
