package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotCompleteARoundThatIsNotInProgress
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import dev.forkhandles.result4k.failureOrNull
import dev.forkhandles.result4k.orThrow
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

@Nested
class CompletingARoundTest {
    @Test
    fun `when a round is completed, a RoundCompleted event is emitted`() {
        val roundNumber = RoundNumber.of(1)

        val game = Arb.newGame.next()
        game.execute(StartRound(roundNumber = roundNumber)).orThrow()
        game.execute(CompleteRound(roundNumber = roundNumber)).orThrow()

        val roundCompletedEvents = game.events.filterIsInstance<RoundCompleted>()
        assert(roundCompletedEvents.single().roundNumber == roundNumber)
    }

    @Test
    fun `cannot complete a round that is not in progress`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                assume(!game.state.roundIsInProgress)

                val command = CompleteRound(game.state.roundNumber)
                assertIs<CannotCompleteARoundThatIsNotInProgress>(game.execute(command).failureOrNull())
            }
        }
    }

    @Test
    @Disabled
    fun `can only complete a round if all tricks are complete`() {
        TODO("not yet implemented")
    }
}
