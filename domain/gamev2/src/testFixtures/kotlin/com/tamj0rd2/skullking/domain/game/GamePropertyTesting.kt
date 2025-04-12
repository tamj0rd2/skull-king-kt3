package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
import com.tamj0rd2.propertytesting.StatsRecorder
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import io.kotest.common.ExperimentalKotest
import io.kotest.property.Arb
import io.kotest.property.Constraints
import io.kotest.property.PropTestConfig
import io.kotest.property.PropertyContext
import io.kotest.property.and
import io.kotest.property.checkAll
import kotlin.time.Duration.Companion.milliseconds

@Deprecated("Should only be used sparingly.")
fun gamePropertyTest(
    playerIdsArb: Arb<Set<PlayerId>>,
    classifications: GameStatistics<*> = None,
    test: context(PropertyContext, StatsRecorder)
    (Set<PlayerId>, List<GameCommand>) -> Unit,
) = propertyTest { statsRecorder ->

    checkAll(ptConfig(), playerIdsArb, Arb.gameCommands) { playerIds, gameCommands ->
        test(this, statsRecorder, playerIds, gameCommands)
    }
}

@OptIn(ExperimentalKotest::class)
fun ptConfig() =
    PropTestConfig(
        maxDiscardPercentage = 99,
        constraints =
            Unit.let {
                val attemptConstraint = Constraints { it.attempts() < 10000 }
                val durationConstraint = Constraints.duration(1000.milliseconds)
                attemptConstraint.and(durationConstraint)
            },
    )

@Deprecated("delete this")
val somePlayers = setOf(PlayerId.random(), PlayerId.random())

fun Game.mustExecute(command: GameCommand) = execute(command).orThrow()
