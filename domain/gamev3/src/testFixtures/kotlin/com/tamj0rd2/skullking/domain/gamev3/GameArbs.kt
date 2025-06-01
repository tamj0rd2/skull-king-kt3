package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.valueOrNull
import dev.forkhandles.values.random
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.resolution.default

object GameArbs {
    val Arb.Companion.command get() = Arb.default<GameCommand>()
    val Arb.Companion.game get() = Arb.gameBuiltFromScratch

    private val Arb.Companion.gameBuiltFromScratch
        get() =
            Arb.bind(
                // TODO: use constants from the domain.
                Arb.set(Arb.playerId, 2..6),
                Arb.list(Arb.command),
            ) { playerIds, commands ->
                commands.fold(Game.new(playerIds)) { result, command ->
                    result.flatMap { it.execute(command) }
                }
            }

    private val Arb.Companion.playerId get() = arbitrary { SomePlayerId.random(it.random) }

    fun <T> Arb<Result4k<T, *>>.validOnly(): Arb<T> = filter { it is Success }.map { it.valueOrNull()!! }
}
