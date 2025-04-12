package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow

fun interface GameInvariantIncludingInitialGameId {
    operator fun invoke(
        initialGameId: GameId,
        game: Game,
    )
}

fun gameInvariant(
    classifications: GameStatistics = None,
    invariant: GameInvariantIncludingInitialGameId,
) {
    @Suppress("DEPRECATION")
    (
        gamePropertyTest(
            validPlayerIdsArb,
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
