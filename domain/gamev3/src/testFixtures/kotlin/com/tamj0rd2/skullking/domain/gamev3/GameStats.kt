package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.checkCoverageExists
import io.kotest.property.PropertyContext

object GameStats {
    fun PropertyContext.checkCoverageForAllGameStates() =
        apply {
            checkCoverageExists(
                "state",
                GameStateName.entries
                    .minus(GameStateName.NotStarted)
                    .filter {
                        when (it) {
                            // TODO: put WIP states in here to help drive development without failing lots of tests.
                            else -> true
                        }
                    }.toSet(),
            )
        }

    fun PropertyContext.collectState(state: GameState) = collect("state", state.name)

    fun PropertyContext.collectState(game: Game) = collectState(game.state)

    fun PropertyContext.collectCommand(command: GameCommand) = collect("command", command::class.simpleName)
}
