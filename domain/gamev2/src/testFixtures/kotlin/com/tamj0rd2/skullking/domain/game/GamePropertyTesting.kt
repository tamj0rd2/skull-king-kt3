package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.PropertyTesting.propertyTest
import dev.forkhandles.result4k.orThrow
import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.Constraints
import io.kotest.property.PropTestConfig
import io.kotest.property.PropertyContext
import io.kotest.property.and
import io.kotest.property.arbitrary.ProvidedArbsBuilder
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import kotlin.time.Duration.Companion.milliseconds

// TODO: make these extensions on Arb.Companion for easier discoverability.
val roundNumberArb = Arb.int().map { RoundNumber.of(it) }
val trickNumberArb = Arb.int().map { TrickNumber.of(it) }
val bidArb = Arb.int().map { Bid.of(it) }

val playerIdArb = Arb.uuid().map { PlayerId.of(it) }
val validPlayerIdsArb = Arb.set(playerIdArb, Game.MINIMUM_PLAYER_COUNT..Game.MAXIMUM_PLAYER_COUNT)
val potentiallyInvalidPlayerIdsArb = Arb.set(playerIdArb)

val gameCommandArb = Arb.bind<GameCommand> { registerTinyTypes() }
val gameCommandsArb = Arb.list(gameCommandArb)

private fun ProvidedArbsBuilder.registerTinyTypes() {
    bind(RoundNumber::class to roundNumberArb)
    bind(TrickNumber::class to trickNumberArb)
    bind(Bid::class to bidArb)
    bind(PlayerId::class to playerIdArb)
}

@OptIn(ExperimentalKotest::class)
@Deprecated("Should only be used sparingly.")
fun gamePropertyTest(
    playerIdsArb: Arb<Set<PlayerId>>,
    classifications: GameStatistics = None,
    test: PropertyContext.(Set<PlayerId>, List<GameCommand>) -> Unit,
) = propertyTest {
    val propertyTestConfig =
        PropTestConfig(
            maxDiscardPercentage = 99,
            constraints =
                Unit.let {
                    val attemptConstraint = Constraints { it.attempts() < 10000 }
                    val durationConstraint = Constraints.duration(1000.milliseconds)
                    attemptConstraint.and(durationConstraint)
                },
        )

    checkAll(propertyTestConfig, playerIdsArb, gameCommandsArb) { playerIds, gameCommands -> test(playerIds, gameCommands) }
        .apply { classifications.check() }
}

fun interface GameInvariant {
    operator fun invoke(game: Game)
}

fun gameInvariant(
    playerIdsArb: Arb<Set<PlayerId>> = validPlayerIdsArb,
    classifications: GameStatistics = None,
    invariant: GameInvariant,
) {
    @Suppress("DEPRECATION")
    gamePropertyTest(playerIdsArb, classifications) { playerIds, gameCommands ->
        val game = Game.new(playerIds).orThrow()

        gameCommands.forEach { command ->
            game.execute(command)
            invariant(game)
        }
    }
}

fun interface GameInvariantIncludingInitialGameId {
    operator fun invoke(
        initialGameId: GameId,
        game: Game,
    )
}

fun gameInvariant(
    classifications: GameStatistics = None,
    invariant: GameInvariantIncludingInitialGameId,
) {
    @Suppress("DEPRECATION")
    gamePropertyTest(validPlayerIdsArb, classifications) { initialPlayers, gameCommands ->
        val game = Game.new(initialPlayers).orThrow()
        val initialGameId = game.id

        gameCommands.forEach { command ->
            game.execute(command)
            invariant(initialGameId, game)
        }
    }
}

fun interface GameStateInvariantIncludingPreviousState {
    operator fun PropertyContext.invoke(
        stateBeforeCommand: GameState,
        stateAfterCommand: GameState,
    )
}

fun gameStateInvariant(
    classifications: GameStatistics = None,
    invariant: GameStateInvariantIncludingPreviousState,
) {
    @Suppress("DEPRECATION")
    gamePropertyTest(validPlayerIdsArb, classifications) { initialPlayers, gameCommands ->
        val game = Game.new(initialPlayers).orThrow()

        gameCommands.forEach { command ->
            val previousState = game.state
            game.execute(command)
            invariant.run { invoke(stateBeforeCommand = previousState, stateAfterCommand = game.state) }
        }
    }
}
