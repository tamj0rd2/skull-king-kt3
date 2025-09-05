package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.random
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.resolution.GlobalArbResolver
import io.kotest.property.resolution.default

object GameArbs {
    val Arb.Companion.game
        get() = Arb.choice(Arb.gameBuiltFromScratch, Arb.reconstitutedGame)

    private val Arb.Companion.gameBuiltFromScratch
        get() =
            Arb.bind(Arb.playerId, Arb.list(Arb.command)) { creator, commands ->
                commands.fold(Game.new(creator)) { game, command -> game.execute(command) }
            }

    private val Arb.Companion.reconstitutedGame
        get() = Arb.gameBuiltFromScratch.map { Game.reconstitute(it.events) }

    private val Arb.Companion.gameId
        get() = arbitrary { GameId.random(it.random) }

    private val Arb.Companion.playerId
        get() = Arb.string().map(PlayerId::parse)

    val Arb.Companion.command
        get() = Arb.default<GameCommand>()

    init {
        GlobalArbResolver.register<PlayerId>(Arb.playerId)
        GlobalArbResolver.register<GameId>(Arb.gameId)
    }
}
