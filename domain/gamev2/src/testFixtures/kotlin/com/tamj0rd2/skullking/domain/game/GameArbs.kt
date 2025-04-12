package com.tamj0rd2.skullking.domain.game

import io.kotest.property.Arb
import io.kotest.property.arbitrary.ProvidedArbsBuilder
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.arbitrary.uuid

val Arb.Companion.roundNumber get() = Arb.int().map { RoundNumber.of(it) }

val Arb.Companion.trickNumber get() = Arb.int().map { TrickNumber.of(it) }

val Arb.Companion.bid get() = Arb.int().map { Bid.of(it) }

val Arb.Companion.playerId get() = Arb.uuid().map { PlayerId.of(it) }

val Arb.Companion.validPlayerIds get() = Arb.set(Arb.playerId, Game.MINIMUM_PLAYER_COUNT..Game.MAXIMUM_PLAYER_COUNT)

val Arb.Companion.potentiallyInvalidPlayerIds get() = Arb.set(Arb.playerId)

val Arb.Companion.gameCommand get() = Arb.bind<GameCommand> { registerTinyTypes() }

val Arb.Companion.gameCommands get() = Arb.list(Arb.gameCommand)

private fun ProvidedArbsBuilder.registerTinyTypes() {
    bind(RoundNumber::class to Arb.roundNumber)
    bind(TrickNumber::class to Arb.trickNumber)
    bind(Bid::class to Arb.bid)
    bind(PlayerId::class to Arb.playerId)
}
