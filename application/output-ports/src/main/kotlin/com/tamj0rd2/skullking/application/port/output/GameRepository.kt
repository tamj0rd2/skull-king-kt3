package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId

interface GameRepository {
    fun load(gameId: GameId): Game

    fun save(game: Game)
}

class GameDoesNotExist : IllegalStateException()
