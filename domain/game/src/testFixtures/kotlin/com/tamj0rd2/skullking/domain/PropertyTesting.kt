package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.merge
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import strikt.api.Assertion
import strikt.assertions.isA

// NOTE: these arbs need to be kept up to date, as/when I add more actions and events.
object GameArbs {
    val playerIdArb = Arb.uuid().map { PlayerId.of(it) }

    private val addPlayerActionArb =
        arbitrary {
            val playerId = playerIdArb.bind()
            GameAction("add player $playerId") { addPlayer(playerId) }
        }

    private val startGameActionArb =
        arbitrary {
            GameAction("start game") { start() }
        }

    val possiblyInvalidGameActionsArb =
        Arb
            .list(
                Arb.choice(
                    addPlayerActionArb,
                    startGameActionArb,
                ),
            ).map(::GameActions)

    val validGameActionsArb =
        arbitrary {
            GameActions(
                buildList {
                    addAll(Arb.set(addPlayerActionArb, 0..MAXIMUM_PLAYER_COUNT).bind())
                },
            )
        }

    val gameActionsArb = possiblyInvalidGameActionsArb.merge(validGameActionsArb)

    val gameArb =
        arbitrary {
            Game.new().also { game ->
                validGameActionsArb.bind().applyAllTo(game)
            }
        }
}

fun propertyTest(block: suspend () -> Unit) = runBlocking(block)

fun <T, E> Assertion.Builder<Result4k<T, E>>.wasSuccessful() = run { isA<Success<*>>() }

data class GameActions(
    private val actions: List<GameAction>,
) {
    val size = actions.size

    fun applyAllTo(game: Game) {
        actions.fold(listOf<GameAction>()) { appliedActions, action ->
            try {
                action.mutate(game)
            } catch (e: GameErrorCode) {
                println("Previously applied actions:\n${appliedActions.joinToString("\n")}\n")
                println("Failed to apply: $action")
                throw e
            }

            appliedActions + action
        }
    }

    fun applyEach(block: (GameAction) -> Unit) = actions.forEach(block)
}

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
