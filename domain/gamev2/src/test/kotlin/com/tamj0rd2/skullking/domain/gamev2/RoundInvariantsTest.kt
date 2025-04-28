package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.assumeThat
import com.tamj0rd2.propertytesting.setMaxDiscardPercentage
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import dev.forkhandles.result4k.Success
import io.kotest.property.Arb
import io.kotest.property.assume
import io.kotest.property.checkAll
import kotlin.test.Test

class RoundInvariantsTest {
    @Test
    fun `when a round starts, the round number increases`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                val initialRoundNumber = game.state.roundNumberInProgress
                assumeThat(initialRoundNumber != null)

                val command = StartRound(initialRoundNumber.next())
                assume(game.execute(command) is Success)

                val latestState = game.state
                assert(latestState.roundNumberInProgress!! > initialRoundNumber)
            }
        }
    }

    @Test
    fun `when a round starts, the round phase is Bidding`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                val currentRoundNumber = game.state.roundNumberInProgress
                assumeThat(currentRoundNumber != null)

                val command = StartRound(currentRoundNumber.next())
                assume(game.execute(command) is Success)

                val latestState = game.state
                assert(latestState.phase == GamePhase.Bidding)
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
        propertyTest { statsRecorder ->
            checkAll(setMaxDiscardPercentage(95), Arb.game, Arb.gameCommand) { game, command ->
                val roundNumberBefore = game.state.roundNumberInProgress
                assumeThat(roundNumberBefore != null)

                val commandResult = game.execute(command)
                val roundNumberNow = game.state.roundNumberInProgress

                assert(roundNumberNow!! >= roundNumberBefore)

                statsRecorder.run {
                    // TODO: round number 1-10 stats are way too low.
                    RoundNumberStatistics.classify(roundNumberBefore)
                    CommandTypeStatistics.classify(command)
                    CommandExecutionStatistics.classify(commandResult)
                }
            }
        }

    @Test
    fun `the round number only ever increases by 1 at a time`() =
        propertyTest { statsRecorder ->
            checkAll(setMaxDiscardPercentage(95), Arb.game, Arb.gameCommand) { game, command ->
                val roundNumberBefore = game.state.roundNumberInProgress
                assumeThat(roundNumberBefore != null)

                val commandResult = game.execute(command)

                val roundNumberNow = game.state.roundNumberInProgress
                assumeThat(roundNumberNow != null)

                val actualIncrease = roundNumberNow.differenceFrom(roundNumberBefore)
                assert(actualIncrease in setOf(0, 1))

                statsRecorder.run {
                    // TODO: round number 1-10 stats are way too low.
                    RoundNumberStatistics.classify(roundNumberBefore)
                    CommandTypeStatistics.classify(command)
                    CommandExecutionStatistics.classify(commandResult)
                }
            }
        }
}
