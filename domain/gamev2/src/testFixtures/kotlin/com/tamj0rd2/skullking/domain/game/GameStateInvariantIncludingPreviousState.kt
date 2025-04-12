package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.PropertyContext

fun interface GameStateInvariantIncludingPreviousState {
    operator fun PropertyContext.invoke(
        stateBeforeCommand: GameState,
        stateAfterCommand: GameState,
    )
}

fun gameStateInvariant(
    classifications: GameStatistics<*> = None,
    invariant: GameStateInvariantIncludingPreviousState,
) {
    @Suppress("DEPRECATION")
    (
        gamePropertyTest(
            Arb.validPlayerIds,
            classifications,
        ) { initialPlayers, gameCommands ->
            val game = Game.new(initialPlayers).orThrow()

            gameCommands.forEach { command ->
                val previousState = game.state
                game.execute(command)
                invariant.run { invoke(stateBeforeCommand = previousState, stateAfterCommand = game.state) }
            }
        }
    )
}
