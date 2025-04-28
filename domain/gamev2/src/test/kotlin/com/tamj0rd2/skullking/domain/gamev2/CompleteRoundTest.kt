package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotCompleteRoundFromCurrentPhase
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.TrickScoring
import io.kotest.property.Arb
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class CompleteRoundTest {
    @Test
    fun `cannot complete a round that is not in the trick scoring phase`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                assume(game.state.phase != TrickScoring)

                val command = CompleteRound(game.state.round.roundNumber)
                assertFailureIs<CannotCompleteRoundFromCurrentPhase>(game.execute(command))
            }
        }
    }

    @Test
    @Disabled
    fun `can only complete a round if all tricks are complete`() {
        TODO("not yet implemented")
    }
}
