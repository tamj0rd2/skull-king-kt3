package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.Classifier
import com.tamj0rd2.propertytesting.StatisticsBase
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import io.kotest.property.PropertyContext

sealed class GameStatistics : StatisticsBase()

data object None : GameStatistics()

@Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName")
data object RoundNumberStatistics : GameStatistics() {
    internal val `round number less than 0` by optional()
    internal val `round number is 0` by required()
    internal val `round number is 1-10` by required()
    internal val `round number greater than 10` by optional()

    context(PropertyContext)
    fun classify(roundNumber: RoundNumber) {
        classify(
            when {
                roundNumber < RoundNumber.none -> `round number less than 0`
                roundNumber == RoundNumber.none -> `round number is 0`
                roundNumber <= RoundNumber.finalRoundNumber -> `round number is 1-10`
                else -> `round number greater than 10`
            },
        )
    }
}

data object CommandTypeStatistics : GameStatistics() {
    override val requiredClassifiers: Set<Classifier> =
        GameCommand::class
            .sealedSubclasses
            .map {
                Classifier(
                    PREFIX + it.simpleName,
                )
            }.toSet()

    context(PropertyContext)
    fun classify(command: GameCommand) {
        classify(PREFIX + command::class.java.simpleName)
    }

    private const val PREFIX = "command type "
}

@Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName")
data object CommandExecutionStatistics : GameStatistics() {
    internal val `command failed` by required()
    internal val `command succeeded` by required()

    context(PropertyContext)
    fun classify(result: Result4k<Unit, GameErrorCode>) {
        classify(
            when (result) {
                is Success -> `command succeeded`
                is Failure -> `command failed`
            },
        )
    }
}
