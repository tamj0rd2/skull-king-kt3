package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.random
import io.kotest.property.Arb
import io.kotest.property.Shrinker
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.single
import io.kotest.property.arbitrary.string
import io.kotest.property.resolution.GlobalArbResolver
import io.kotest.property.resolution.default

object GameArbs {
    val Arb.Companion.game
        get() = Arb.gameBuiltFromScratch

    private val Arb.Companion.gameBuiltFromScratch
        get() =
            arbitrary(GameShrinker) { rs ->
                val creator = Arb.playerId.single(rs)
                val commands = Arb.list(Arb.command, range = 0..15).single(rs) // Limit max commands for better shrinking
                commands.fold(Game.new(creator)) { game, command -> game.execute(command) }
            }

    private val Arb.Companion.gameId
        get() = arbitrary { GameId.random(it.random) }

    private val Arb.Companion.playerId
        get() = Arb.string(maxSize = 20).map(PlayerId::parse)

    val Arb.Companion.command
        get() = Arb.default<GameCommand>()

    init {
        GlobalArbResolver.register<PlayerId>(Arb.playerId)
        GlobalArbResolver.register<GameId>(Arb.gameId)
    }
}

private object GameShrinker : Shrinker<Game> {
    override fun shrink(value: Game): List<Game> {
        // If the game has only the initial GameCreated event, we can't shrink further
        if (value.events.size <= 1) return emptyList()
        return listOf(Game.reconstitute(value.events.dropLast(1)))
    }
}
