package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.propertytesting.Classifier
import com.tamj0rd2.propertytesting.StatisticsBase
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

sealed class GameStatistics<T> : StatisticsBase<T>()

@Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName")
data object RoundNumberStatistics : GameStatistics<RoundNumber>() {
    internal val `round number less than 0` by optional()
    internal val `round number is 0` by required()
    internal val `round number is 1-10` by required()
    internal val `round number greater than 10` by optional()

    override fun classifyData(data: RoundNumber) =
        when {
            data < RoundNumber.none -> `round number less than 0`
            data == RoundNumber.none -> `round number is 0`
            data <= RoundNumber.last -> `round number is 1-10`
            else -> `round number greater than 10`
        }
}

data object CommandTypeStatistics : GameStatistics<GameCommand>() {
    override val requiredClassifiers: Set<Classifier> =
        GameCommand::class
            .sealedSubclasses
            .map { Classifier(PREFIX + it.simpleName) }
            .toSet()

    override fun classifyData(data: GameCommand) = Classifier(PREFIX + data::class.java.simpleName)

    private const val PREFIX = "command type "
}

@Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName")
data object CommandExecutionStatistics : GameStatistics<Result4k<Unit, GameErrorCode>>() {
    internal val `command failed` by required()
    internal val `command succeeded` by required()

    override fun classifyData(data: Result4k<Unit, GameErrorCode>) =
        when (data) {
            is Success -> `command succeeded`
            is Failure -> `command failed`
        }
}

@Suppress("MemberVisibilityCanBePrivate", "ObjectPropertyName")
data object EventCountStatistics : GameStatistics<List<GameEvent>>() {
    internal val `event count 0` by optional()
    internal val `event count 1-10` by required()
    internal val `event count 10-20` by required()
    internal val `event count more than 20` by optional() // TODO: this should be increased and made required.

    override fun classifyData(data: List<GameEvent>): Classifier {
        val size = data.size
        return when (size) {
            0 -> `event count 0`
            in 1..<10 -> `event count 1-10`
            in 10..<20 -> `event count 10-20`
            else -> `event count more than 20`
        }
    }
}
