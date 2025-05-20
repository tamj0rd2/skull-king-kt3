package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.assumeSuccess
import com.tamj0rd2.propertytesting.setMaxDiscardPercentage
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.CannotStartATrickFromCurrentPhase
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.gamev2.GamePhase.TrickScoring
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterIsInstance
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@Nested
class StartTrickTest {
    @Test
    fun `successfully starting a trick transitions the phase to Trick Taking`() {
        propertyTest {
            val startTrickArb = Arb.gameCommand.filterIsInstance<StartTrick>()

            checkAll(setMaxDiscardPercentage(98), Arb.game, startTrickArb) { initial, command ->
                val updated = initial.execute(command).assumeSuccess()
                val currentPhase = updated.state.phase
                assert(currentPhase == GamePhase.TrickTaking)
            }
        }
    }

    @Test
    fun `cannot start a trick outside of the bidding or trick scoring phases`() =
        propertyTest {
            val startTrickArb = Arb.gameCommand.filterIsInstance<StartTrick>()

            checkAll(setMaxDiscardPercentage(45), Arb.game, startTrickArb) { initial, command ->
                assume(initial.state.phase !in setOf(Bidding, TrickScoring))

                assertFailureIs<CannotStartATrickFromCurrentPhase>(initial.execute(command), "coming from phase ${initial.state.phase}")
            }
        }

    @Test
    @Disabled
    fun `cannot start a trick that has already started`() {
        TODO("not yet implemented")
    }
}
