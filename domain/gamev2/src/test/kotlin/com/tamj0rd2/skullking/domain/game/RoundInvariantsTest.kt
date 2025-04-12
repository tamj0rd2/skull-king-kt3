package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import dev.forkhandles.result4k.Success
import io.kotest.property.assume
import kotlin.test.Test

class RoundInvariantsTest {
    @Test
    fun `when a round starts, the round number increases`() {
        gameInvariant { game ->
            val initialRoundNumber = game.state.roundNumber
            val command = StartRound(initialRoundNumber.next)
            assume(game.execute(command) is Success)

            val latestState = game.state
            assert(latestState.roundNumber > initialRoundNumber)
        }
    }

    @Test
    fun `when a round starts, the round is in progress`() {
        gameInvariant { game ->
            val command = StartRound(game.state.roundNumber.next)
            assume(game.execute(command) is Success)

            val latestState = game.state
            assert(latestState.roundIsInProgress)
        }
    }

    @Test
    fun `the round number is never greater than 10`() =
        gameInvariant { game ->
            assert(game.state.roundNumber <= RoundNumber.finalRoundNumber)
        }

    @Test
    fun `the round number never decreases`() =
        gameStateInvariant(RoundNumberStatistics) { stateBeforeCommand, stateAfterCommand ->
            val roundNumberBefore = stateBeforeCommand.roundNumber
            val roundNumberNow = stateAfterCommand.roundNumber
            assert(roundNumberNow >= roundNumberBefore)

            RoundNumberStatistics.classify(roundNumberBefore)
        }

    @Test
    fun `the round number only ever increases by 1 at a time`() =
        gameStateInvariant(RoundNumberStatistics) { stateBeforeCommand, stateAfterCommand ->
            val roundNumberBefore = stateBeforeCommand.roundNumber
            val roundNumberNow = stateAfterCommand.roundNumber
            val actualIncrease = roundNumberNow.value - roundNumberBefore.value
            assert(actualIncrease == 0 || actualIncrease == 1)

            RoundNumberStatistics.classify(roundNumberBefore)
        }
}
