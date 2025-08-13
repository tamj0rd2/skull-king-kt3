package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

interface GameRepositoryContract {

    val gameRepository: GameRepository

    @Test
    fun `can save and load a game`() {
        val game = Game.new(GameId.random())
        gameRepository.save(game)

        val loadedGame = gameRepository.load(game.id)
        expectThat(loadedGame).isEqualTo(game)

        val allGames = gameRepository.loadAll()
        expectThat(allGames.map { it.id }).contains(game.id)
    }
}
