package com.tamj0rd2.skullking.domain.gamev3

sealed interface GamePhase {
    data object NotStarted : GamePhase

    data object AwaitingNextRound : GamePhase

    data object Bidding : GamePhase
}
