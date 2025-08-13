package com.tamj0rd2.skullking.domain.game

data class Game(val id: GameId) {
    companion object {
        fun new(id: GameId): Game {
            return Game(id)
        }
    }
}
