package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.PropertyTesting.gameInvariant
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class CompletingARoundTest {
    @Test
    fun `a maximum of 10 rounds can be completed`() =
        gameInvariant { game ->
            val roundCompletedEvents = game.state.events.filterIsInstance<RoundCompleted>()
            assert(roundCompletedEvents.size <= 10)
        }

    @Test
    fun `when a round is completed, a RoundCompleted event is emitted`() {
        val command =
            CompleteRound(
                roundNumber = RoundNumber.of(1),
            )

        val game = Game.new(somePlayers).orThrow()
        game.mustExecute(command)

        val roundCompletedEvents = game.state.events.filterIsInstance<RoundCompleted>()
        assert(roundCompletedEvents.single().roundNumber == command.roundNumber)
    }

    @Test
    @Disabled
    fun `can only complete a round if all tricks are complete`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot complete a round that has already been completed`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot complete a round that hasn't started`() {
        TODO("not yet implemented")
    }
}
