package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.gamev2.values.TrickNumber
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Nested
class CompleteTrickTest {
    @Test
    fun `example - when a trick is completed, the phase changes to TrickScoring`() {
        val command = CompleteTrick(trickNumber = TrickNumber.One)

        val game =
            Arb.newGame
                .next()
                .execute(command)
                .orThrow()

        assertEquals(GamePhase.TrickScoring, game.state.phase)
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
