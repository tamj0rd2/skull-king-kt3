package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartingATrick {
    @Test
    fun `when a trick is started, a TrickStartedEvent is emitted`() {
        val command =
            StartTrick(
                trickNumber = TrickNumber.of(1),
            )

        val game = Game.new(somePlayers).orThrow()
        game.mustExecute(command)

        val trickStartedEvent =
            game.state.events
                .filterIsInstance<TrickStarted>()
                .single()
        assert(trickStartedEvent.trickNumber == command.trickNumber)
    }

    @Test
    @Disabled
    fun `cannot start a trick that has already started`() {
        TODO("not yet implemented")
    }
}
