package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.CardPlayed
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class PlayACardTest {
    @Test
    fun `when a card is played, a CardPlayed event is emitted`() {
        val command =
            PlayACard(
                card = CannedCard,
                actor = PlayerId.random(),
            )

        val game = Arb.newGame.next()
        game.execute(command).orThrow()

        val cardPlayedEvent =
            game.events
                .filterIsInstance<CardPlayed>()
                .single()
        assert(cardPlayedEvent.playedBy == command.actor)
        assert(cardPlayedEvent.card == command.card)
    }

    @Test
    @Disabled
    fun `a card can only be played during a trick`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `a card can only be played by the player whose turn it is`() {
        TODO("not yet implemented")
    }
}
