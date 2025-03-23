package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickCompleted
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@Nested
class CompletingATrick {
    @Test
    fun `when a trick is completed, a TrickCompleted event is emitted`() {
        val command =
            CompleteTrick(
                trickNumber = TrickNumber.of(1),
            )

        val game = Game(somePlayers)
        game.mustExecute(command)

        val trickCompletedEvent = game.events.filterIsInstance<TrickCompleted>().single()
        expectThat(trickCompletedEvent) {
            get { gameId }.isEqualTo(game.id)
            get { trickNumber }.isEqualTo(command.trickNumber)
        }
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
