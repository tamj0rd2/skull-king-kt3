package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.GameArbs.validGameEventsArb
import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.GameCreated
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import com.tamj0rd2.skullking.domain.propertyTest
import dev.forkhandles.values.random
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

abstract class GameRepositoryContract {
    protected abstract val gameRepository: GameRepository

    @Test
    fun `all previously saved events for a game can be loaded`() =
        propertyTest {
            checkAll(validGameEventsArb) { events ->
                val gameId = Game.new().also(gameRepository::save).id

                // TODO: this is really bad
                val eventsAdaptedForThisGame = events.map { it.withGameId(gameId) }
                eventsAdaptedForThisGame.drop(1).forEach { event ->
                    val game = gameRepository.load(gameId)
                    when (event) {
                        is GameCreated -> error("shouldn't replay this one")
                        is PlayerJoined -> game.addPlayer(event.playerId)
                    }
                    gameRepository.save(game)
                }

                val loadedGameAfterAllSaves = gameRepository.load(gameId)
                expectThat(loadedGameAfterAllSaves.events).isEqualTo(eventsAdaptedForThisGame)
            }
        }

    @Test
    fun `a game that has not been saved cannot be loaded`() {
        expectThrows<IllegalStateException> { gameRepository.load(GameId.random()) }
    }
}

private fun GameEvent.withGameId(gameId: GameId) =
    when (this) {
        is GameCreated -> copy(gameId = gameId)
        is PlayerJoined -> copy(gameId = gameId)
    }
