package com.tamj0rd2.skullking.application.ports.output

import com.tamj0rd2.skullking.application.ports.output.VersionedAtLoad.Companion.map
import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameEvent
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotNull

interface GameRepositoryContract {

    val gameRepository: GameRepository

    @Test
    fun `can save and load a game`() {
        val game = Game.new(GameId.random(), PlayerId("test-player"))
        gameRepository.save(game, Version.initial)

        val versionedGame = gameRepository.load(game.id)
        expectThat(versionedGame).isNotNull().and {
            get { aggregate.id }.isEqualTo(game.id)
            get { aggregate.events }.isEqualTo(game.events)
        }

        val allGames = gameRepository.findAll()
        expectThat(allGames.map { it.id }).contains(game.id)
    }

    @Test
    fun `when a game is saved, only new events are emitted`() {
        val subscriber = SpyGameEventSubscriber()
        val gameId = GameId.random()
        val gameBeforeInitialSave = Game.new(gameId, PlayerId("john"))

        gameRepository.subscribe(subscriber)
        gameRepository.save(gameBeforeInitialSave, Version.initial)
        eventually { expectThat(subscriber.events).isEqualTo(gameBeforeInitialSave.events) }

        subscriber.reset()
        val gameAfterLoad = gameRepository.load(gameBeforeInitialSave.id)!!
        gameRepository.save(gameAfterLoad.map { it.addPlayer(PlayerId("jane")) })

        eventually {
            expectThat(subscriber.events)
                .containsExactly(GameEvent.PlayerJoined(gameId, PlayerId("jane")))
        }
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

        eventually { expectThat(subscriber.callCount).isGreaterThan(1) }
    }
}
