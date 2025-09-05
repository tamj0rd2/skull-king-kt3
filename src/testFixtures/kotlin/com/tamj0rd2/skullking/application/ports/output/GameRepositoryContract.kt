package com.tamj0rd2.skullking.application.ports.output

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThan
import com.natpryce.hamkrest.hasElement
import com.tamj0rd2.skullking.domain.Version
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test

interface GameRepositoryContract {

    val gameRepository: GameRepository

    @Test
    fun `can save and load a game`() {
        val game = Game.new(GameId.random(), PlayerId("test-player"))
        gameRepository.save(game, Version.initial)

        val (loadedGame, _) = gameRepository.load(game.id)!!
        assertThat(loadedGame, equalTo(game))

        val allGames = gameRepository.findAll()
        assertThat(allGames.map { it.id }, hasElement(game.id))
    }

    @Test
    fun `when a game is saved, only new events are emitted`() {
        val subscriber = SpyGameEventSubscriber()
        val gameId = GameId.random()
        val gameBeforeInitialSave = Game.new(gameId, PlayerId("john"))

        gameRepository.subscribe(subscriber)
        gameRepository.save(gameBeforeInitialSave, Version.initial)
        eventually { assertThat(subscriber.events, equalTo(gameBeforeInitialSave.events)) }

        subscriber.reset()
        val (gameAfterLoad, version) = gameRepository.load(gameBeforeInitialSave.id)!!
        gameRepository.save(gameAfterLoad.addPlayer(PlayerId("jane")), version)

        eventually { assertThat(subscriber.events, equalTo(listOf(GameEvent.PlayerJoined(gameId, PlayerId("jane"))))) }
    }

    @Test
    fun `if notifying of a game event fails, it will be retried to achieve at least once delivery`() {
        val subscriber =
            object : GameEventSubscriber {
                var callCount = 0

                override fun notify(event: GameEvent) {
                    callCount++
                    if (callCount == 1) {
                        throw RuntimeException("Simulated failure")
                    }
                    println("success")
                }
            }
        gameRepository.subscribe(subscriber)

        val game = Game.new(GameId.random(), PlayerId("test-player"))
        gameRepository.save(game, Version.initial)

        eventually { assertThat(subscriber.callCount, greaterThan(1)) }
    }
}
