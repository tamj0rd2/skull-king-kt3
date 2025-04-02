package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.PropertyTesting.gameInvariant
import dev.forkhandles.result4k.orThrow
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartingARoundTest {
    @Test
    @Disabled
    fun `a maximum of 10 rounds can be started`() =
        gameInvariant { game ->
            val roundStartedEvents = game.events.filterIsInstance<RoundStarted>()
            assert(roundStartedEvents.size <= 10)
        }

    @Test
    fun `when a round has started, a round started event is emitted`() {
        val game = Game.new(somePlayers).orThrow()
        val command = StartRound(RoundNumber.of(1))
        game.mustExecute(command)

        val roundStartedEvent =
            game.events
                .filterIsInstance<RoundStarted>()
                .single()
        assert(roundStartedEvent.roundNumber == command.roundNumber)

        val dealtCardsPerPlayer = roundStartedEvent.dealtCards.perPlayer
        dealtCardsPerPlayer.onEachIndexed { index, (_, cards) ->
            assert(cards.size == command.roundNumber.value) { "incorrect cards for player ${index + 1}/${somePlayers.size}" }
        }
    }
}
