package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb

fun interface GameInvariant {
    operator fun invoke(game: Game)
}

@Deprecated("do not use.")
fun gameInvariant(
    playerIdsArb: Arb<Set<PlayerId>> = Arb.validPlayerIds,
    classifications: GameStatistics = None,
    invariant: GameInvariant,
) {
    gamePropertyTest(
        playerIdsArb,
        classifications,
    ) { playerIds, gameCommands ->
        val game = Game.new(playerIds).orThrow()

        gameCommands.forEach { command ->
            game.execute(command)
            invariant(game)
        }
    }
}
