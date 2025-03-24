package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@Nested
class CompletingARound {
    @Test
    fun `when a round is completed, a RoundCompleted event is emitted`() {
        val command =
            CompleteRound(
                roundNumber = RoundNumber.of(1),
            )

        val game = Game.new(somePlayers).orThrow()
        game.mustExecute(command)

        val roundCompletedEvent =
            game.state.events
                .filterIsInstance<RoundCompleted>()
                .single()
        expectThat(roundCompletedEvent) {
            get { gameId }.isEqualTo(game.id)
            get { roundNumber }.isEqualTo(command.roundNumber)
        }
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
