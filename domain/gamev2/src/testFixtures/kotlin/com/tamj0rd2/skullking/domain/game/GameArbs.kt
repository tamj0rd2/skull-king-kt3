package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.fold
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.ofResult4k
import io.kotest.property.Arb
import io.kotest.property.arbitrary.ProvidedArbsBuilder
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filterIsInstance
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid

// NOTE: constrained to speed up generation
private val Arb.Companion.roundNumber
    get() =
        Arb
            .int(RoundNumber.first.value..RoundNumber.last.value)
            .map { RoundNumber.ofResult4k(it) }
            .successesOnly()

val Arb.Companion.trickNumber get() = Arb.int().map { TrickNumber.of(it) }

val Arb.Companion.bid get() = Arb.int().map { Bid.of(it) }

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
    bind(RoundNumber::class to Arb.roundNumber)
    bind(TrickNumber::class to Arb.trickNumber)
    bind(Bid::class to Arb.bid)
    bind(PlayerId::class to Arb.playerId)
}

private fun <T, E> Arb<Result4k<T, E>>.successesOnly() = filterIsInstance<Success<T>>().map { it.orThrow() }
