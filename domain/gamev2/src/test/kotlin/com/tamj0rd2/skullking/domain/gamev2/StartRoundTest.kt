package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.assumeThat
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotStartARoundThatIsAlreadyInProgress
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.checkAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartRoundTest {
    @Test
    fun `when a round has started, a round started event is emitted`() {
        val game = Arb.newGame.next()
        val command = StartRound(RoundNumber.One)
        game.execute(command).orThrow()

        val roundStartedEvent =
            game.events
                .filterIsInstance<RoundStarted>()
                .single()
        assert(roundStartedEvent.roundNumber == command.roundNumber)

        val dealtCardsPerPlayer = roundStartedEvent.dealtCards.perPlayer
        dealtCardsPerPlayer.onEachIndexed { index, (_, cards) ->
            assert(
                cards.size == command.roundNumber.totalCardsToDeal,
            ) { "incorrect cards for player ${index + 1}/${Arb.validPlayerIds.next().size}" }
        }
    }

    @Test
    fun `cannot start a round that is already in progress`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                val round = game.state.round
                assumeThat(round is Round.InProgress)

                val command = StartRound(round.roundNumber)
                assertFailureIs<CannotStartARoundThatIsAlreadyInProgress>(game.execute(command))
            }
        }
}
