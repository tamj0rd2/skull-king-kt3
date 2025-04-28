package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
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
                val initialRoundNumber = game.state.roundInProgress.roundNumber
                val command = StartRound(initialRoundNumber.next)
                assume(game.execute(command) is Success)

                val latestState = game.state
                assert(latestState.roundInProgress.roundNumber > initialRoundNumber)
            }
        }
    }

    @Test
    fun `when a round starts, the round phase is Bidding`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                val command = StartRound(game.state.roundInProgress.roundNumber.next)
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
                assert(game.state.roundInProgress.roundNumber <= RoundNumber.last)
            }
        }

    @Test
    fun `the round number never decreases`() =
        propertyTest { statsRecorder ->
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val roundNumberBefore = game.state.roundInProgress.roundNumber
                val commandResult = game.execute(command)
                val roundNumberNow = game.state.roundInProgress.roundNumber
                assert(roundNumberNow >= roundNumberBefore)

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
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val roundNumberBefore = game.state.roundInProgress.roundNumber
                val commandResult = game.execute(command)
                val roundNumberNow = game.state.roundInProgress.roundNumber

                val actualIncrease = roundNumberNow.value - roundNumberBefore.value
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
