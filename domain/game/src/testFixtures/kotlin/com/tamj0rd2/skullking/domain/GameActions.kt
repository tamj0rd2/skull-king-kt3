package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameErrorCode
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.orThrow

data class GameActions(
    private val actions: List<GameAction>,
) {
    val size = actions.size

    fun applyAllTo(game: Game) {
        actions.fold(listOf<GameAction>()) { appliedActions, action ->
            try {
                action.mutate(game)
            } catch (e: GameErrorCode) {
                println("Previously applied actions:\n${appliedActions.joinToString("\n")}\n")
                println("Failed to apply: $action")
                throw e
            }

            appliedActions + action
        }
    }

    fun applyEach(block: (GameAction) -> Unit) = actions.forEach(block)
}

data class GameAction(
    private val description: String,
    private val mutation: Game.() -> Result4k<Unit, GameErrorCode>,
) {
    override fun toString(): String = description

    fun mutate(game: Game) = applyTo(game)

    fun applyTo(game: Game) = mutation(game).orThrow()

    override fun equals(other: Any?): Boolean {
        if (other !is GameAction) return false
        return this.description == other.description
    }

    override fun hashCode(): Int = description.hashCode()
}
