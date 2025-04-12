package com.tamj0rd2.skullking.domain.game

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
