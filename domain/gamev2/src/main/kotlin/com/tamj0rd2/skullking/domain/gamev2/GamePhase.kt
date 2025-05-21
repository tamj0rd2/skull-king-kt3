package com.tamj0rd2.skullking.domain.gamev2

sealed interface GamePhase {
    data object None : GamePhase

    data object AwaitingNextRound : GamePhase

    data object Bidding : GamePhase

    data object TrickTaking : GamePhase

    data object TrickScoring : GamePhase
}
