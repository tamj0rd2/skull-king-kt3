package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteGame
import com.tamj0rd2.skullking.domain.game.GameEvent.GameCompleted
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@Nested
class CompletingAGame {
    @Test
    fun `when a game is completed, a GameCompleted event is emitted`() {
        val command = CompleteGame

        val game = Game(somePlayers)
        game.mustExecute(command)

        val gameCompletedEvent =
            game.state.events
                .filterIsInstance<GameCompleted>()
                .single()
        expectThat(gameCompletedEvent) {
            get { gameId }.isEqualTo(game.id)
        }
    }

    @Test
    @Disabled
    fun `cannot complete the game if the final round has not been completed`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot complete a game that is already complete`() {
        TODO("not yet implemented")
    }
}
