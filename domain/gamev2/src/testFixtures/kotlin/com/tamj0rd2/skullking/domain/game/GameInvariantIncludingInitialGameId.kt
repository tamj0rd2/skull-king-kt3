package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb

fun interface GameInvariantIncludingInitialGameId {
    operator fun invoke(
        initialGameId: GameId,
        game: Game,
    )
}

@Deprecated("do not use :D")
fun gameInvariant(
    classifications: GameStatistics<*> = None,
    invariant: GameInvariantIncludingInitialGameId,
) {
    @Suppress("DEPRECATION")
    (
        gamePropertyTest(
            Arb.validPlayerIds,
            classifications,
        ) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()
            val initialGameId = game.id

            gameCommands.forEach { command ->
                game.execute(command)
                invariant(initialGameId, game)
            }
        }
    )
}
