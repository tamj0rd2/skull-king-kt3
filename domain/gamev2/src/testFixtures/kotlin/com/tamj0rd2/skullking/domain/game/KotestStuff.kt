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

fun interface GameInvariant {
    operator fun invoke(game: Game)
}

fun interface GameInvariantIncludingInitialPlayers {
    operator fun invoke(
        players: Set<PlayerId>,
        game: Game,
    )
}

fun interface GameInvariantIncludingInitialGameId {
    operator fun invoke(
        initialGameId: GameId,
        game: Game,
    )
}

@Suppress("DeprecatedCallableAddReplaceWith")
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

    fun propertyTest(block: suspend () -> Unit) {
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

    @Deprecated("Should only be used sparingly.")
    fun gamePropertyTest(
        playerIdsArb: Arb<Set<PlayerId>>,
        test: PropertyContext.(Set<PlayerId>, List<GameCommand>) -> Unit,
    ) = propertyTest { checkAll(playerIdsArb, gameCommandsArb, test) }

    // TODO: also add a second check around reconstituting the entity from events.
    fun gameInvariant(
        playerIdsArb: Arb<Set<PlayerId>> = validPlayerIdsArb,
        invariant: GameInvariant,
    ) {
        @Suppress("DEPRECATION")
        gamePropertyTest(playerIdsArb) { playerIds, gameCommands ->
            Game
                .new(playerIds)
                .orThrow()
                .testInvariantHoldsWhenExecuting(gameCommands, invariant)
        }
    }

    // TODO: also add a second check around reconstituting the entity from events.
    fun gameInvariant(invariant: GameInvariantIncludingInitialPlayers) {
        @Suppress("DEPRECATION")
        gamePropertyTest(validPlayerIdsArb) { initialPlayers, gameCommands ->
            Game
                .new(initialPlayers)
                .orThrow()
                .testInvariantHoldsWhenExecuting(gameCommands) { invariant(initialPlayers, it) }
        }
    }

    // TODO: also add a second check around reconstituting the entity from events.
    fun gameInvariant(invariant: GameInvariantIncludingInitialGameId) {
        @Suppress("DEPRECATION")
        gamePropertyTest(validPlayerIdsArb) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()
            val initialGameId = game.id
            game.testInvariantHoldsWhenExecuting(gameCommands) { invariant(initialGameId, it) }
        }
    }

    private fun Game.testInvariantHoldsWhenExecuting(
        commands: List<GameCommand>,
        invariant: GameInvariant,
    ) {
        commands.forEach { command ->
            execute(command)
            invariant(this)
        }
    }
}
