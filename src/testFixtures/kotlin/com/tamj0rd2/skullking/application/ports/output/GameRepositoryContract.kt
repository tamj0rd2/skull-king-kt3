package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

interface GameRepositoryContract {

    val gameRepository: GameRepository

    @Test
    fun `can save and load a game`() {
        val game = Game.new(GameId.random(), PlayerId("test-player"))
        gameRepository.save(game)

        val loadedGame = gameRepository.load(game.id)
        expectThat(loadedGame).isEqualTo(game)

        val allGames = gameRepository.findAll()
        expectThat(allGames.map { it.id }).contains(game.id)
    }

    @Test
    fun `when a game is saved, new events are emitted`() {
        val game =
            Game.new(GameId.random(), PlayerId("test-player")).addPlayer(PlayerId("test-player"))
        val subscriber = SpyGameEventSubscriber()
        gameRepository.subscribe(subscriber)

        gameRepository.save(game)
        eventually { expectThat(subscriber.events).isEqualTo(game.newEvents) }
    }
}
