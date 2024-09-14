package com.tamj0rd2.skullking.port.output

import com.tamj0rd2.skullking.application.port.output.GameRepository
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import dev.forkhandles.values.random
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import strikt.api.expectThat
import strikt.assertions.isEqualTo

abstract class GameRepositoryContract {
    protected abstract val gameRepository: GameRepository

    @Property
    fun `all previously saved changes for a game can be retrieved`(
        @ForAll @IntRange(min = 0, max = MAXIMUM_PLAYER_COUNT) timesToSave: Int,
    ) {
        val gameId = GameId.random()

        val allChangesMadeAcrossSaves =
            buildList {
                repeat(timesToSave) {
                    val loadedGame = gameRepository.load(gameId)
                    loadedGame.addPlayer(PlayerId.random())
                    gameRepository.save(loadedGame)
                    @Suppress("UNCHECKED_CAST")
                    addAll(loadedGame.changes as List<PlayerJoined>)
                }
            }

        val loadedGameAfterAllSaves = gameRepository.load(gameId)
        expectThat(loadedGameAfterAllSaves.players).isEqualTo(allChangesMadeAcrossSaves.map { it.playerId })
    }
}
