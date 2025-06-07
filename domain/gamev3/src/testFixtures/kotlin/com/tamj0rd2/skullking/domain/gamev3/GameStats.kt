package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.checkCoverageExists
import io.kotest.property.PropertyContext

object GameStats {
    fun GameStateName.isExpectedToBeTransitionable() =
        when (this) {
            GameStateName.TrickTaking -> false // TODO: remove this to drive further implementation
            else -> true
        }

    fun PropertyContext.checkCoverageForAllGameStates() =
        apply {
            checkCoverageExists(
                "state",
                GameStateName.entries
                    .minus(GameStateName.NotStarted)
                    .filter { it.isExpectedToBeTransitionable() }
                    .toSet(),
            )
        }

    fun PropertyContext.collectState(state: GameState) = collect("state", state.name)

    fun PropertyContext.collectState(game: Game) = collectState(game.state)

    fun PropertyContext.collectCommand(command: GameCommand) = collect("command", command::class.simpleName)

    fun PropertyContext.checkCoverageForCommands() =
        collect(GameCommand::class.sealedSubclasses.filter { it.isFinal }.map { it.simpleName })
}
