package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.GameRepository
import com.tamj0rd2.skullking.domain.game.Game

class InMemoryGameRepository() : GameRepository {
    override fun save(game: Game) {
        TODO("Not yet implemented")
    }
}
