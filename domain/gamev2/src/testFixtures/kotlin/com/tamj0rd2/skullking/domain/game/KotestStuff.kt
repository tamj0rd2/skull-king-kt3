package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.ProvidedArbsBuilder
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import io.kotest.property.checkAll
import java.io.OutputStream
import java.io.PrintStream

val roundNumberArb = Arb.int().map { RoundNumber.of(it) }
val trickNumberArb = Arb.int().map { TrickNumber.of(it) }
val bidArb = Arb.int().map { Bid.of(it) }
val playerIdArb = Arb.uuid().map { PlayerId.of(it) }
val validPlayerIdsArb = Arb.set(playerIdArb, MINIMUM_PLAYER_COUNT..MAXIMUM_PLAYER_COUNT)

val gameCommandArb = Arb.bind<GameCommand> { registerTinyTypes() }
val validGameCommandsArb =
    Arb
        .list(gameCommandArb)
        .filter { it.count { it is StartRound } < 10 }
        .filter { it.count { it is CompleteRound } < 10 }

fun ProvidedArbsBuilder.registerTinyTypes() {
    bind(RoundNumber::class to roundNumberArb)
    bind(TrickNumber::class to trickNumberArb)
    bind(Bid::class to bidArb)
    bind(PlayerId::class to playerIdArb)
}

fun propertyTest(block: suspend () -> Unit) {
    val originalOutputStream = System.out
    try {
        // makes kotest shut up.
        System.setOut(PrintStream(OutputStream.nullOutputStream()))
        runBlocking(block)
    } finally {
        System.setOut(originalOutputStream)
    }
}

fun gameInvariant(invariant: (Game) -> Unit = { TODO("define this invariant.") }) =
    propertyTest {
        checkAll(validPlayerIdsArb, validGameCommandsArb) { playerIds, gameCommands ->
            val game = Game(playerIds)

            gameCommands.forEach { command ->
                game.execute(command)
                game.run(invariant)
            }
        }

        // TODO: also add a second check around reconstituting the entity from events.
    }
