package com.tamj0rd2.skullking.domain.model.game

import com.tamj0rd2.skullking.domain.model.PlayerId

data class GameState(
    val players: List<PlayerId>,
) {
    companion object {
        internal fun new() =
            GameState(
                players = emptyList(),
            )
    }
}

data object Card
