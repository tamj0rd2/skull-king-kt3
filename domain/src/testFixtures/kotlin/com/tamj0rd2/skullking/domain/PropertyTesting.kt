package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.GameCreated
import com.tamj0rd2.skullking.domain.model.GameErrorCode
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
import io.kotest.property.arbitrary.flatMap
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import strikt.api.Assertion
import strikt.assertions.isA

// NOTE: these arbs need to be kept up to date, as/when I add more actions and events.
object GameArbs {
    private val gameIdArb = Arb.uuid(allowNilValue = false).map { GameId.of(it) }
    val playerIdArb = Arb.uuid().map { PlayerId.of(it) }

    private fun gameCreatedArb(gameId: GameId) = arbitrary { GameCreated(gameId) }

    private fun playerJoinedArb(gameId: GameId) = playerIdArb.map { playerId -> PlayerJoined(gameId, playerId) }

    val gameEventsArb =
        Arb.list(
            gameIdArb.flatMap { gameId ->
                Arb.choice(
                    gameCreatedArb(gameId),
                    playerJoinedArb(gameId),
                )
            },
        )

    val validGameEventsArb =
        arbitrary {
            val gameId = gameIdArb.bind()

            buildList {
                add(gameCreatedArb(gameId).bind())
                addAll(Arb.set(playerJoinedArb(gameId), 0..MAXIMUM_PLAYER_COUNT).bind())
            }
        }

    private val playerJoinedActionArb =
        arbitrary {
            val playerId = playerIdArb.bind()
            GameAction("add player $playerId") { addPlayer(playerId) }
        }

    val validGameActionsArb =
        arbitrary {
            buildList {
                addAll(Arb.set(playerJoinedActionArb, 0..MAXIMUM_PLAYER_COUNT).bind())
            }
        }

    val gameArb =
        arbitrary {
            Game.new().also { game ->
                validGameActionsArb.bind().forEach { it.mutate(game) }
            }
        }
}

fun propertyTest(block: suspend () -> Unit) = runBlocking(block)

fun <T, E> Assertion.Builder<Result4k<T, E>>.wasSuccessful() = run { isA<Success<*>>() }

data class GameAction(
    private val description: String,
    private val mutation: Game.() -> Result4k<Unit, GameErrorCode>,
) {
    override fun toString(): String = description

    fun mutate(game: Game) = mutation(game).orThrow()

    override fun equals(other: Any?): Boolean {
        if (other !is GameAction) return false
        return this.description == other.description
    }

    override fun hashCode(): Int = description.hashCode()
}
