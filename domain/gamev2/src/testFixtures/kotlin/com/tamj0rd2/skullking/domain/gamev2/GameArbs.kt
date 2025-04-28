package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.fold
import com.tamj0rd2.skullking.domain.gamev2.values.Bid
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import com.tamj0rd2.skullking.domain.gamev2.values.TrickNumber
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.ofResult4k
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.ProvidedArbsBuilder
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filterIsInstance
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.exhaustive.filter
import io.kotest.property.exhaustive.map

// NOTE: constrained otherwise generation is too slow
private val Exhaustive.Companion.roundNumber
    get() = exhaustive(RoundNumber.all.toList())

val Exhaustive.Companion.trickNumber
    get() = exhaustive(TrickNumber.first.value..TrickNumber.last.value, TrickNumber)

// NOTE: constrained otherwise generation is too slow
val Exhaustive.Companion.bid
    get() = exhaustive(Bid.allPossibleBids.toList())

val Arb.Companion.playerId get() = Arb.uuid().map { PlayerId.of(it) }

val Arb.Companion.validPlayerIds get() = Arb.set(Arb.playerId, Game.MINIMUM_PLAYER_COUNT..Game.MAXIMUM_PLAYER_COUNT)

val Arb.Companion.gameCommand get() = Arb.bind<GameCommand> { registerTinyTypes() }

val Arb.Companion.gameCommands get() = Arb.list(Arb.gameCommand)

val Arb.Companion.newGameResult get() = Arb.validPlayerIds.map { Game.new(it) }

val Arb.Companion.newGame get() = Arb.newGameResult.successesOnly()

val Arb.Companion.gameResult
    get() =
        Arb.bind(
            Arb.newGameResult,
            Arb.gameCommands,
        ) { gameResult, gameCommands ->
            gameResult.fold(gameCommands, Game::execute)
        }

val Arb.Companion.game
    get() = Arb.gameResult.successesOnly()

private fun ProvidedArbsBuilder.registerTinyTypes() {
    bind(RoundNumber::class to Exhaustive.roundNumber.toArb())
    bind(TrickNumber::class to Exhaustive.trickNumber.toArb())
    bind(Bid::class to Exhaustive.bid.toArb())
    bind(PlayerId::class to Arb.playerId)
}

private fun <T, E> Arb<Result4k<T, E>>.successesOnly() = filterIsInstance<Success<T>>().map { it.orThrow() }

private fun <T, E : Throwable> Exhaustive<Result4k<T, E>>.successesOnly() = filter { it is Success }.map { it.orThrow() }

private fun <T : Value<Int>> exhaustive(
    range: IntRange,
    factory: IntValueFactory<T>,
) = range
    .toList()
    .exhaustive()
    .map(factory::ofResult4k)
    .successesOnly()
