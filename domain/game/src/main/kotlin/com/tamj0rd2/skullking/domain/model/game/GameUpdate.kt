package com.tamj0rd2.skullking.domain.model.game

import com.tamj0rd2.skullking.domain.model.PlayerId

sealed interface GameUpdate {
    data class PlayerJoined(
        val playerId: PlayerId,
    ) : GameUpdate

    data object GameStarted : GameUpdate
}
