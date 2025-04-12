package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
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
                val initialRoundNumber = game.state.roundNumber
                val command = StartRound(initialRoundNumber.next)
                assume(game.execute(command) is Success)

                val latestState = game.state
                assert(latestState.roundNumber > initialRoundNumber)
            }
        }
    }

    @Test
    fun `when a round starts, the round is in progress`() {
        propertyTest {
            checkAll(Arb.game) { game ->
                val command = StartRound(game.state.roundNumber.next)
                assume(game.execute(command) is Success)

                val latestState = game.state
                assert(latestState.roundIsInProgress)
            }
        }
    }

    @Test
    fun `the round number is never greater than 10`() =
        propertyTest {
            checkAll(Arb.game) { game ->
                assert(game.state.roundNumber <= RoundNumber.finalRoundNumber)
            }
        }

    @Test
    fun `the round number never decreases`() =
        propertyTest { statsRecorder ->
            checkAll(Arb.game, Arb.gameCommand) { game, command ->
                val roundNumberBefore = game.state.roundNumber
                val commandResult = game.execute(command)
                val roundNumberNow = game.state.roundNumber
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
                val roundNumberBefore = game.state.roundNumber
                val commandResult = game.execute(command)
                val roundNumberNow = game.state.roundNumber

                val actualIncrease = roundNumberNow.value - roundNumberBefore.value
                assert(actualIncrease == 0 || actualIncrease == 1)

                statsRecorder.run {
                    // TODO: round number 1-10 stats are way too low.
                    RoundNumberStatistics.classify(roundNumberBefore)
                    CommandTypeStatistics.classify(command)
                    CommandExecutionStatistics.classify(commandResult)
                }
            }
        }
}
