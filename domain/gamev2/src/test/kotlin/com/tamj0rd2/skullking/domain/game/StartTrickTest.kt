package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.assertFailureIs
import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.setMaxDiscardPercentage
import com.tamj0rd2.skullking.domain.game.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.game.GameErrorCode.CannotStartATrickFromCurrentPhase
import com.tamj0rd2.skullking.domain.game.GamePhase.Bidding
import com.tamj0rd2.skullking.domain.game.GamePhase.TrickScoring
import dev.forkhandles.result4k.Success
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

            checkAll(setMaxDiscardPercentage(98), Arb.game, startTrickArb) { game, command ->
                assume(game.execute(command) is Success)

                val currentPhase = game.state.phase
                assert(currentPhase == GamePhase.TrickTaking)
            }
        }
    }

    @Test
    fun `cannot start a trick outside of the bidding or trick scoring phases`() =
        propertyTest {
            val startTrickArb = Arb.gameCommand.filterIsInstance<StartTrick>()

            checkAll(setMaxDiscardPercentage(45), Arb.game, startTrickArb) { game, command ->
                assume(game.state.phase !in setOf(Bidding, TrickScoring))

                assertFailureIs<CannotStartATrickFromCurrentPhase>(game.execute(command), "coming from phase ${game.state.phase}")
            }
        }

    @Test
    @Disabled
    fun `cannot start a trick that has already started`() {
        TODO("not yet implemented")
    }
}
