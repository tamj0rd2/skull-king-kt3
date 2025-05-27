package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import dev.forkhandles.values.ofResult4k
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid
import io.kotest.property.resolution.default

val Arb.Companion.playerId get() = Arb.uuid().map { SomePlayerId.ofResult4k(it) }.validOnly()
val Arb.Companion.game get() = Arb.set(playerId).map { Game.new(it) }
val Arb.Companion.command get() = Arb.default<GameCommand>()

fun <T> Arb<Result4k<T, *>>.validOnly(): Arb<T> = filter { it is Success }.map { it.valueOrNull()!! }
