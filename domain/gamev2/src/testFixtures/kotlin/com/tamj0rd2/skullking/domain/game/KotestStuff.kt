package com.tamj0rd2.skullking.domain.game

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
import java.io.OutputStream
import java.io.PrintStream
import kotlin.text.RegexOption.MULTILINE

val roundNumberArb = Arb.int().map { RoundNumber.of(it) }
val trickNumberArb = Arb.int().map { TrickNumber.of(it) }
val bidArb = Arb.int().map { Bid.of(it) }

val playerIdArb = Arb.uuid().map { PlayerId.of(it) }
val validPlayerIdsArb = Arb.set(playerIdArb, Game.MINIMUM_PLAYER_COUNT..Game.MAXIMUM_PLAYER_COUNT)
val potentiallyInvalidPlayerIdsArb = Arb.set(playerIdArb)

val gameCommandArb = Arb.bind<GameCommand> { registerTinyTypes() }
val gameCommandsArb = Arb.list(gameCommandArb)

fun ProvidedArbsBuilder.registerTinyTypes() {
    bind(RoundNumber::class to roundNumberArb)
    bind(TrickNumber::class to trickNumberArb)
    bind(Bid::class to bidArb)
    bind(PlayerId::class to playerIdArb)
}

typealias GamePropertyTest = PropertyContext.(Set<PlayerId>, List<GameCommand>) -> Unit
typealias GameInvariant = (Game) -> Unit

object PropertyTesting {
    init {
        System.setProperty("kotest.assertions.collection.print.size", "10")
    }

    private val stackTracePartsToIgnore =
        setOf(
            PropertyTesting::class.qualifiedName!!,
            "io.kotest",
            "kotlin.coroutines",
            "kotlin.test",
        )

    private fun Throwable.rootCause(): Throwable = cause?.rootCause() ?: this

    private fun Throwable.cleanedStackTrace(): Array<StackTraceElement> =
        stackTrace.filter { element -> stackTracePartsToIgnore.none { element.className.startsWith(it) } }.toTypedArray()

    private fun propertyTest(block: suspend () -> Unit) {
        val originalOutputStream = System.out
        try {
            // makes kotest shut up.
            System.setOut(PrintStream(OutputStream.nullOutputStream()))
            runBlocking(block)
        } catch (e: AssertionError) {
            val args =
                "Arg \\d+: .*"
                    .toRegex(MULTILINE)
                    .findAll(e.message!!)
                    .mapNotNull { it.groupValues.firstOrNull() }
                    .map { it.substringBefore(" (shrunk from") }
                    .toList()

            val seed = "Repeat this test by using seed (-?\\d+)".toRegex().find(e.message!!)?.groupValues?.lastOrNull()

            val rootCause = e.rootCause().also { it.stackTrace = it.cleanedStackTrace() }
            throw AssertionError("Property failed (seed: $seed)\n\n${args.joinToString("\n")}\n${rootCause.message}", rootCause).also {
                it.stackTrace =
                    rootCause.stackTrace
            }
        } finally {
            System.setOut(originalOutputStream)
        }
    }

    fun gameInvariant(
        playerIdsArb: Arb<Set<PlayerId>>,
        gameCommandsArb: Arb<List<GameCommand>> = com.tamj0rd2.skullking.domain.game.gameCommandsArb,
        test: GamePropertyTest,
    ) = propertyTest { checkAll(playerIdsArb, gameCommandsArb, test) }

    fun gameInvariant(
        playerIdsArb: Arb<Set<PlayerId>> = validPlayerIdsArb,
        gameCommandsArb: Arb<List<GameCommand>> = com.tamj0rd2.skullking.domain.game.gameCommandsArb,
        invariant: GameInvariant,
    ) {
        gameInvariant(playerIdsArb, gameCommandsArb) { playerIds, gameCommands ->
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
}
