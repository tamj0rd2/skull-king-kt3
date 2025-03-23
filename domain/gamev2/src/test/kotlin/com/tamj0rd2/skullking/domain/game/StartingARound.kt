package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty

@Nested
class StartingARound {
    @Test
    fun `when a round has started, a round started event is emitted`() {
        val game = Game(somePlayers)
        game.mustExecute(StartRound(RoundNumber.of(1)))

        val roundStartedEvent =
            game.state.events
                .filterIsInstance<RoundStarted>()
                .single()

        expectThat(roundStartedEvent) {
            get { roundNumber }.isEqualTo(RoundNumber.of(1))
            get { dealtCards.perPlayer.values }
                .describedAs("cards dealt to each player")
                .isNotEmpty()
                .all { hasSize(roundStartedEvent.roundNumber.value) }
        }
    }
}
