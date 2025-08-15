package com.tamj0rd2.skullking.domain.game

data class Game private constructor(val id: GameId, val creator: PlayerId) {
    companion object {
        fun new(id: GameId, createdBy: PlayerId): Game {
            return Game(id = id, creator = createdBy)
        }
    }
}
