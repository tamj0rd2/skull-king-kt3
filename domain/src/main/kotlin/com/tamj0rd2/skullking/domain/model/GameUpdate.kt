package com.tamj0rd2.skullking.domain.model

sealed interface GameUpdate {
    data class PlayerJoined(
        val playerId: PlayerId,
    ) : GameUpdate
}
