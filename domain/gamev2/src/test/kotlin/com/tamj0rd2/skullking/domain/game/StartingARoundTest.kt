package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartARoundThatIsAlreadyInProgress
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.failureOrNull
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

@Nested
class StartingARoundTest {
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
                assume(game.state.roundIsInProgress)

                val command = StartRound(game.state.roundNumber)
                assertIs<CannotStartARoundThatIsAlreadyInProgress>(game.execute(command).failureOrNull())
            }
        }
}
