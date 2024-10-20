package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.GameActionArbs.validGameActionsArb
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.Version
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
    fun `modifying, saving and loading a game multiple times results in the same state as just modifying the game in memory`() =
        propertyTest {
            checkAll(validGameActionsArb) { actions ->
                val gameModifiedInMemoryOnly = Game.new().also(gameRepository::save)
                val gameId = gameModifiedInMemoryOnly.id

                actions.applyAllTo(gameModifiedInMemoryOnly)
                actions.applyEach { action ->
                    val modifiedGame = gameRepository.load(gameId).apply(action::mutate)
                    gameRepository.save(modifiedGame)
                }

                val gameThatWasSavedAndLoaded = gameRepository.load(gameId)
                expectThat(gameThatWasSavedAndLoaded.id).isEqualTo(gameModifiedInMemoryOnly.id)
                expectThat(gameThatWasSavedAndLoaded.events).isEqualTo(gameModifiedInMemoryOnly.events)
                expectThat(gameThatWasSavedAndLoaded.state).isEqualTo(gameModifiedInMemoryOnly.state)
                // TODO: these 2 assertions deserve their own test.
                expectThat(gameThatWasSavedAndLoaded.loadedVersion).isEqualTo(Version.of(actions.size))
                expectThat(gameModifiedInMemoryOnly.loadedVersion).isEqualTo(Version.NONE)
            }
        }

    @Test
    fun `a game that has not been saved cannot be loaded`() {
        expectThrows<GameDoesNotExist> { gameRepository.load(GameId.random()) }
    }
}
