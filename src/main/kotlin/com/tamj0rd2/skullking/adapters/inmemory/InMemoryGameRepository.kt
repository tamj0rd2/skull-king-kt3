package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.GameRepository
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId

class InMemoryGameRepository() : GameRepository {
    private val games = mutableMapOf<GameId, Game>()

    override fun save(game: Game) {
        games[game.id] = game
    }

    override fun load(gameId: GameId): Game? {
        return games[gameId]
    }

    override fun findAll(): List<Game> {
        return games.values.toList()
    }
}
