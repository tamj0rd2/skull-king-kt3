package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.game.Game

interface GameRepository : SaveGamePort

interface SaveGamePort {
    fun save(game: Game)
}
