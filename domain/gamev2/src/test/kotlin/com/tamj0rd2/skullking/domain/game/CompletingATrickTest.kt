package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class CompletingATrickTest {
    @Test
    fun `when a trick is completed, a TrickCompleted event is emitted`() {
        val command = CompleteTrick(trickNumber = TrickNumber.of(1))

        val game = Game.new(somePlayers).orThrow()
        game.mustExecute(command)

        val trickCompletedEvents = game.events.filterIsInstance<TrickCompleted>()
        assert(trickCompletedEvents.single().trickNumber == command.trickNumber)
    }

    @Test
    @Disabled
    fun `can only complete a trick if all players have played a card`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot complete a trick that has already been completed`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot complete a trick that hasn't started`() {
        TODO("not yet implemented")
    }
}
