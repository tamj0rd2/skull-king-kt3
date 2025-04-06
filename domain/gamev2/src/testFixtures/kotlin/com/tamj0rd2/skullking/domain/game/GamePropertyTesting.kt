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

@Suppress("PropertyName", "ObjectPrivatePropertyName")
open class GameClassifications : PropertyTesting.ClassificationsBase() {
    val `has passed assumptions without game commands` by classification()
    val `has passed assumptions with some game commands` by classification()

    object ForRoundNumber : GameClassifications() {
        private val `round number is 0` by classification()
        private val `round number is not 0` by classification()

        fun PropertyContext.classifyRoundNumber(roundNumber: RoundNumber) {
            classify(
                condition = roundNumber == RoundNumber.none,
                trueLabel = `round number is 0`,
                falseLabel = `round number is not 0`,
            )
        }
    }
}

@OptIn(ExperimentalKotest::class)
@Deprecated("Should only be used sparingly.")
fun gamePropertyTest(
    playerIdsArb: Arb<Set<PlayerId>>,
    classifications: GameClassifications = GameClassifications(),
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

    checkAll(propertyTestConfig, playerIdsArb, gameCommandsArb) { playerIds, gameCommands ->
        test(playerIds, gameCommands)
        classify(
            condition = gameCommands.isEmpty(),
            trueLabel = classifications.`has passed assumptions without game commands`,
            falseLabel = classifications.`has passed assumptions with some game commands`,
        )
    }.also {
        val expectedClassifiers = classifications.classifiers
        val actualClassifiers =
            it
                .classifications()
                .keys
                .sorted()
                .toSet()

        check(actualClassifiers.containsAll(expectedClassifiers)) {
            """
            The test did not exercise all expected classifications.
            Expected: $expectedClassifiers
            Actual:   $actualClassifiers
            Missed:   ${expectedClassifiers - actualClassifiers}
            """.trimIndent()
        }
    }
}

fun interface GameInvariant {
    operator fun invoke(game: Game)
}

fun gameInvariant(
    playerIdsArb: Arb<Set<PlayerId>> = validPlayerIdsArb,
    classifications: GameClassifications = GameClassifications(),
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
    classifications: GameClassifications = GameClassifications(),
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
    classifications: GameClassifications = GameClassifications(),
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
