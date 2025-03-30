package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteGame
import com.tamj0rd2.skullking.domain.game.GameEvent.GameCompleted
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class CompletingAGame {
    @Test
    fun `when a game is completed, a GameCompleted event is emitted`() {
        val command = CompleteGame

        val game = Game.new(somePlayers).orThrow()
        game.mustExecute(command)

        val events = game.state.events
        assert(events.last() is GameCompleted)
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
