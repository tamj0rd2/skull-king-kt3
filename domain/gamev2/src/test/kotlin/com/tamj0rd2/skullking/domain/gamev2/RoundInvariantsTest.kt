package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.assumeSuccess
import com.tamj0rd2.propertytesting.assumeThat
import com.tamj0rd2.propertytesting.setMaxDiscardPercentage
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import io.kotest.property.Arb
import io.kotest.property.checkAll
import kotlin.test.Test

class RoundInvariantsTest {
    @Test
    fun `when a round starts, the round number increases`() {
        propertyTest {
            checkAll(Arb.game) { initial ->
                val initialRoundNumber = initial.state.roundNumberInProgress
                assumeThat(initialRoundNumber != null)

                val updated = initial.execute(StartRound(initialRoundNumber.next())).assumeSuccess()
                assert(updated.state.roundNumberInProgress!! > initialRoundNumber)
            }
        }
    }

    @Test
    fun `when a round starts, the round phase is Bidding`() {
        propertyTest {
            checkAll(Arb.game) { initial ->
                val currentRoundNumber = initial.state.roundNumberInProgress
                assumeThat(currentRoundNumber != null)

                val updated = initial.execute(StartRound(currentRoundNumber.next())).assumeSuccess()
                assert(updated.state.phase == GamePhase.Bidding)
            }
        }
    }

    @Test
    fun `the round number is never greater than 10`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                val currentRoundNumber = game.state.roundNumberInProgress
                assumeThat(currentRoundNumber != null)
                assert(currentRoundNumber <= RoundNumber.Ten)
            }
        }

    @Test
    fun `the round number never decreases`() =
        propertyTest {
            checkAll(setMaxDiscardPercentage(95), Arb.game, Arb.gameCommand) { initial, command ->
                val roundNumberBefore = initial.state.roundNumberInProgress
                assumeThat(roundNumberBefore != null)

                val roundNumberNow =
                    initial
                        .execute(command)
                        .assumeSuccess()
                        .state.roundNumberInProgress
                assert(roundNumberNow!! >= roundNumberBefore)
            }
        }

    @Test
    fun `the round number only ever increases by a maximum of 1 at a time`() =
        propertyTest {
            checkAll(setMaxDiscardPercentage(95), Arb.game, Arb.gameCommand) { initial, command ->
                val roundNumberBefore = initial.state.roundNumberInProgress
                assumeThat(roundNumberBefore != null)

                val roundNumberNow =
                    initial
                        .execute(command)
                        .assumeSuccess()
                        .state.roundNumberInProgress
                assumeThat(roundNumberNow != null)

                val actualIncrease = roundNumberNow.differenceFrom(roundNumberBefore)
                assert(actualIncrease in setOf(0, 1))
            }
        }
}
