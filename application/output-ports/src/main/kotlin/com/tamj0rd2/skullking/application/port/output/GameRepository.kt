package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameId

interface GameRepository {
    fun load(gameId: GameId): Game

    fun save(game: Game)
}

class GameDoesNotExist : IllegalStateException()