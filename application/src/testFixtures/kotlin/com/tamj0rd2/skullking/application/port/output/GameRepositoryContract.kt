package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.model.Game
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import dev.forkhandles.values.random
import net.jqwik.api.Example
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

abstract class GameRepositoryContract {
    protected abstract val gameRepository: GameRepository

    @Property
    fun `all previously saved changes for a game can be loaded`(
        @ForAll @IntRange(min = 0, max = MAXIMUM_PLAYER_COUNT) timesToSave: Int,
    ) {
        val game = Game.new()
        gameRepository.save(game)

        val playersAddedAcrossSaves =
            buildList {
                repeat(timesToSave) {
                    val loadedGame = gameRepository.load(game.id)
                    PlayerId.random().also { playerId ->
                        add(playerId)
                        loadedGame.addPlayer(playerId)
                    }
                    gameRepository.save(loadedGame)
                }
            }

        val loadedGameAfterAllSaves = gameRepository.load(game.id)
        expectThat(loadedGameAfterAllSaves.state.players).isEqualTo(playersAddedAcrossSaves)
    }

    @Example
    fun `a game that has not been saved cannot be loaded`() {
        expectThrows<IllegalStateException> { gameRepository.load(GameId.random()) }
    }
}
