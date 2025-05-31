package com.tamj0rd2.skullking.domain.gamev3

sealed interface GameState {
    data object NotStarted : GameState

    data object AwaitingNextRound : GameState

    data object Bidding : GameState
}
