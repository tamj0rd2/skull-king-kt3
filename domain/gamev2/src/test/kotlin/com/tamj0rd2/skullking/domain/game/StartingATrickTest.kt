package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.game.GameEvent.TrickStarted
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartingATrickTest {
    @Test
    fun `when a trick is started, a TrickStartedEvent is emitted`() {
        val command =
            StartTrick(
                trickNumber = TrickNumber.of(1),
            )

        val game = Arb.newGame.next()
        game.execute(command).orThrow()

        val trickStartedEvent =
            game.events
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
