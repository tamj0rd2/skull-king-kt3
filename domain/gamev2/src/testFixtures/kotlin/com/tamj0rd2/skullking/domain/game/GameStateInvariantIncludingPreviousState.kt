package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.StatsRecorder
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.PropertyContext

fun interface GameStateInvariantIncludingPreviousState {
    context(StatsRecorder) // TODO: remove this once I've converted all tests to use checkAll directly
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
