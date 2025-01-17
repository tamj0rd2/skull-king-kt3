package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Game
import com.tamj0rd2.skullking.domain.game.GameActionArbs
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.mustExecute
import com.tamj0rd2.skullking.domain.game.propertyTest
import dev.forkhandles.values.random
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

abstract class GameRepositoryContract {
    protected abstract val gameRepository: GameRepository

    @Test
    fun `modifying, saving and loading a game multiple times results in the same state as just modifying the game in memory`() =
        propertyTest {
            checkAll(GameActionArbs.validGameActionsArb) { actions ->
                val gameModifiedInMemoryOnly = Game.new(PlayerId.random()).also(gameRepository::save)
                val gameId = gameModifiedInMemoryOnly.id

                actions.forEach { gameModifiedInMemoryOnly.mustExecute(it) }
                actions.forEach {
                    val game = gameRepository.load(gameId)
                    game.mustExecute(it)
                    gameRepository.save(game)
                }

                val gameThatWasSavedAndLoaded = gameRepository.load(gameId)
                expectThat(gameThatWasSavedAndLoaded.id).isEqualTo(gameModifiedInMemoryOnly.id)
                expectThat(gameThatWasSavedAndLoaded.allEvents).isEqualTo(gameModifiedInMemoryOnly.allEvents)
                expectThat(gameThatWasSavedAndLoaded.state).isEqualTo(gameModifiedInMemoryOnly.state)
            }
        }

    @Test
    fun `a game that has not been saved cannot be loaded`() {
        expectThrows<GameDoesNotExist> { gameRepository.load(GameId.random()) }
    }
}
