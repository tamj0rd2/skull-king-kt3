package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@Nested
class StartingATrick {
    @Test
    fun `when a trick is started, a TrickStartedEvent is emitted`() {
        val command =
            StartTrick(
                trickNumber = TrickNumber.of(1),
            )

        val game = Game(somePlayers)
        game.mustExecute(command)

        val trickStartedEvent =
            game.state.events
                .filterIsInstance<TrickStarted>()
                .single()
        expectThat(trickStartedEvent) {
            get { gameId }.isEqualTo(game.id)
            get { trickNumber }.isEqualTo(command.trickNumber)
        }
    }

    @Test
    @Disabled
    fun `cannot start a trick that has already started`() {
        TODO("not yet implemented")
    }
}
