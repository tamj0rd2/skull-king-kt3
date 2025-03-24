package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.failureOrNull
import dev.forkhandles.result4k.orThrow
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.PropertyContext
import io.kotest.property.arbitrary.ProvidedArbsBuilder
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import io.kotest.property.statistics.withCoveragePercentages
import strikt.api.Assertion.Builder
import strikt.assertions.isA
import strikt.assertions.isNotNull
import java.io.OutputStream
import java.io.PrintStream

val roundNumberArb = Arb.int().map { RoundNumber.of(it) }
val trickNumberArb = Arb.int().map { TrickNumber.of(it) }
val bidArb = Arb.int().map { Bid.of(it) }
val playerIdArb = Arb.uuid().map { PlayerId.of(it) }
val playerIdsArb = Arb.set(playerIdArb)
private val validPlayerIdsArb = Arb.set(playerIdArb, Game.MINIMUM_PLAYER_COUNT..Game.MAXIMUM_PLAYER_COUNT)

val gameCommandArb = Arb.bind<GameCommand> { registerTinyTypes() }
val gameCommandsArb = Arb.list(gameCommandArb)

fun ProvidedArbsBuilder.registerTinyTypes() {
    bind(RoundNumber::class to roundNumberArb)
    bind(TrickNumber::class to trickNumberArb)
    bind(Bid::class to bidArb)
    bind(PlayerId::class to playerIdArb)
}

fun <T> propertyTest(block: suspend () -> T): T {
    val originalOutputStream = System.out
    try {
        // makes kotest shut up.
        System.setOut(PrintStream(OutputStream.nullOutputStream()))
        return runBlocking(block)
    } finally {
        System.setOut(originalOutputStream)
    }
}

typealias GamePropertyTest = PropertyContext.(Set<PlayerId>, List<GameCommand>) -> Unit

fun gamePropertyTest(
    playerIdsArb: Arb<Set<PlayerId>> = validPlayerIdsArb,
    gameCommandsArb: Arb<List<GameCommand>> = com.tamj0rd2.skullking.domain.game.gameCommandsArb,
    expectedClassifications: Set<Any?> = emptySet(),
    test: GamePropertyTest,
) = propertyTest {
    withCoveragePercentages(expectedClassifications.associateWith { 1.0 }) {
        checkAll(playerIdsArb, gameCommandsArb) { playerIds, gameCommands -> test(playerIds, gameCommands) }
    }
}

typealias GameInvariant = (Game) -> Unit

fun gameInvariant(invariant: GameInvariant) {
    gamePropertyTest { playerIds, gameCommands ->
        val game = Game.new(playerIds).orThrow()
        game.testInvariantHoldsWhenExecuting(gameCommands, invariant)
        // TODO: also add a second check around reconstituting the entity from events.
    }
}

fun Game.testInvariantHoldsWhenExecuting(
    commands: List<GameCommand>,
    invariant: GameInvariant,
) {
    commands.forEach { command ->
        execute(command)
        run(invariant)
    }
}

inline fun <reified T : GameErrorCode> Builder<Result<Game, GameErrorCode>>.isAGameErrorCodeOfType() {
    get { failureOrNull() }.isNotNull().isA<T>()
}
