package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.valueOrNull
import dev.forkhandles.values.ofResult4k
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import io.kotest.property.resolution.default

val Arb.Companion.playerId get() = Arb.uuid().map { SomePlayerId.ofResult4k(it) }.validOnly()

val Arb.Companion.command get() = Arb.default<GameCommand>()

val Arb.Companion.gameWithPotentiallyInvalidPlayers get() =
    Arb.bind(
        Arb.set(Arb.playerId),
        Arb.list(Arb.command),
        ::buildGame,
    )

val Arb.Companion.game get() =
    Arb.bind(
        // TODO: use constants from the domain.
        Arb.set(Arb.playerId, 2..6),
        Arb.list(Arb.command),
        ::buildGame,
    )

fun <T> Arb<Result4k<T, *>>.validOnly(): Arb<T> = filter { it is Success }.map { it.valueOrNull()!! }

private fun buildGame(
    playerIds: Set<SomePlayerId>,
    commands: List<GameCommand>,
): GameResult =
    commands.fold(Game.new(playerIds)) { result, command ->
        result.flatMap { it.execute(command) }
    }
