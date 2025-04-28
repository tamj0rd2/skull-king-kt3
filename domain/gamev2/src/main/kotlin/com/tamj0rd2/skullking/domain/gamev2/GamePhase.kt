package com.tamj0rd2.skullking.domain.gamev2

enum class GamePhase {
    None,
    AwaitingNextRound,

    // TODO: these ones only apply when the round is in progress
    Bidding,
    TrickTaking,
    TrickScoring,
}
