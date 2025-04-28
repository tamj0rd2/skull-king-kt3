package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartARoundThatIsAlreadyInProgress
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.game.GamePhase.TrickScoring
import com.tamj0rd2.skullking.domain.game.GamePhase.TrickTaking
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartRoundTest {
    @Test
    fun `when a round has started, a round started event is emitted`() {
        val game = Arb.newGame.next()
        val command = StartRound(RoundNumber.of(1))
        game.execute(command).orThrow()

        val roundStartedEvent =
            game.events
                .filterIsInstance<RoundStarted>()
                .single()
        assert(roundStartedEvent.roundNumber == command.roundNumber)

        val dealtCardsPerPlayer = roundStartedEvent.dealtCards.perPlayer
        dealtCardsPerPlayer.onEachIndexed { index, (_, cards) ->
            assert(cards.size == command.roundNumber.value) { "incorrect cards for player ${index + 1}/${Arb.validPlayerIds.next().size}" }
        }
    }

    @Test
    fun `cannot start a round that is already in progress`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                // TODO: a clear sign that these particular phases should live with "RoundInProgress"
                assume(game.state.phase in setOf(Bidding, TrickTaking, TrickScoring))

                val command = StartRound(game.state.roundInProgress.roundNumber)
                assertFailureIs<CannotStartARoundThatIsAlreadyInProgress>(game.execute(command))
            }
        }
}
