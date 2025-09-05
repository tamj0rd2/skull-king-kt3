package com.tamj0rd2.skullking.application.repositories

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasElement
import com.natpryce.hamkrest.hasSize
import com.tamj0rd2.skullking.adapters.inmemory.InMemoryGameEventStore
import com.tamj0rd2.skullking.application.ports.output.random
import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameCommand
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import org.junit.jupiter.api.Test

class GameRepositoryTest {
    private val eventStore = InMemoryGameEventStore()
    private val gameRepository = GameRepository(eventStore)

    @Test
    fun `can save and load a game`() {
        val game = Game.new(PlayerId.of("test-player"))
        gameRepository.save(game, Version.initial)

        val (loadedGame, _) = gameRepository.load(game.id)
        assertThat(loadedGame, equalTo(game))

        eventually {
            // this one's a projection
            val allGames = gameRepository.findAll()
            assertThat(allGames.map { it.id }, hasElement(game.id))
        }
    }

    @Test
    fun `when a game is saved, only new events are written to avoid duplication`() {
        val initialGame = Game.new(PlayerId.of("john"))
        val gameId = initialGame.id
        gameRepository.save(initialGame, Version.initial)

        val (loadedGame, version) = gameRepository.load(gameId)
        val modifiedGame = loadedGame.execute(GameCommand.AddPlayer(PlayerId.random()))
        assertThat(modifiedGame.events, hasSize(equalTo(initialGame.events.size + 1)))

        gameRepository.save(modifiedGame, version)
        assertThat(eventStore.read(gameId), equalTo(modifiedGame.events))
    }
}
