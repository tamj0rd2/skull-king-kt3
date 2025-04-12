package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.PropertyTesting.propertyTest
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

@OptIn(ExperimentalKotest::class)
@Deprecated("Should only be used sparingly.")
fun gamePropertyTest(
    playerIdsArb: Arb<Set<PlayerId>>,
    classifications: GameStatistics = None,
    test: PropertyContext.(Set<PlayerId>, List<GameCommand>) -> Unit,
) = propertyTest {
    val propertyTestConfig =
        PropTestConfig(
            maxDiscardPercentage = 99,
            constraints =
                Unit.let {
                    val attemptConstraint = Constraints { it.attempts() < 10000 }
                    val durationConstraint = Constraints.duration(1000.milliseconds)
                    attemptConstraint.and(durationConstraint)
                },
        )

    checkAll(propertyTestConfig, playerIdsArb, Arb.gameCommands) { playerIds, gameCommands -> test(playerIds, gameCommands) }
        .apply { classifications.check() }
}

@Deprecated("delete this")
val somePlayers = setOf(PlayerId.random(), PlayerId.random())

fun Game.mustExecute(command: GameCommand) = execute(command).orThrow()
