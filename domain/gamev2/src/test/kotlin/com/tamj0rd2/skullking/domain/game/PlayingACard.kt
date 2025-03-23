package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.game.GameEvent.CardPlayed
import dev.forkhandles.values.random
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@Nested
class PlayingACard {
    @Test
    fun `when a card is played, a CardPlayed event is emitted`() {
        val command =
            PlayACard(
                card = CannedCard,
                actor = PlayerId.random(),
            )

        val game = Game(somePlayers)
        game.mustExecute(command)

        val cardPlayedEvent = game.events.filterIsInstance<CardPlayed>().single()
        expectThat(cardPlayedEvent) {
            get { gameId }.isEqualTo(game.id)
            get { playedBy }.isEqualTo(command.actor)
            get { card }.isEqualTo(command.card)
        }
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
