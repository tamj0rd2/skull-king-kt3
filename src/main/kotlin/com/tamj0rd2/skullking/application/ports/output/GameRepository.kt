package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId

interface GameRepository : SaveGamePort, LoadGamePort, FindGamesPort

interface SaveGamePort {
    fun save(game: Game)
}

interface LoadGamePort {
    fun load(gameId: GameId): Game?
}

interface FindGamesPort {
    fun findAll(): List<Game>
}
