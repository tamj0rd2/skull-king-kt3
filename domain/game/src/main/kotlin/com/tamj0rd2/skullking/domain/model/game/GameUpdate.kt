package com.tamj0rd2.skullking.domain.model.game

sealed interface GameUpdate {
    data class PlayerJoined(
        val playerId: PlayerId,
    ) : GameUpdate

    data object GameStarted : GameUpdate

    data class CardDealt(
        val card: Card,
    ) : GameUpdate
}
