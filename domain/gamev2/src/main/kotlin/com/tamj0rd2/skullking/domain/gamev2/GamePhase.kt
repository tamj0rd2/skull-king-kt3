package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber

sealed interface GamePhase {
    data object None : GamePhase

    data object AwaitingNextRound : GamePhase

    data object Bidding : GamePhase

    data object TrickTaking : GamePhase

    data class TrickScoring(
        val roundNumber: RoundNumber,
    ) : GamePhase
}
