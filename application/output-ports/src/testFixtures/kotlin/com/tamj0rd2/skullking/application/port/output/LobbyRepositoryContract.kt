package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.domain.game.Lobby
import com.tamj0rd2.skullking.domain.game.LobbyActionArbs
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.mustExecute
import com.tamj0rd2.skullking.domain.game.propertyTest
import dev.forkhandles.values.random
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

interface LobbyRepositoryContract {
    val lobbyRepository: LobbyRepository

    @Test
    fun `modifying, saving and loading a lobby multiple times results in the same state as just modifying the lobby in memory`() =
        propertyTest {
            checkAll(LobbyActionArbs.validLobbyCommandsArb) { actions ->
                val lobbyModifiedInMemoryOnly = Lobby.new(PlayerId.random()).also(lobbyRepository::save)
                val lobbyId = lobbyModifiedInMemoryOnly.id

                actions.forEach { lobbyModifiedInMemoryOnly.mustExecute(it) }
                actions.forEach {
                    val game = lobbyRepository.load(lobbyId)
                    game.mustExecute(it)
                    lobbyRepository.save(game)
                }

                val gameThatWasSavedAndLoaded = lobbyRepository.load(lobbyId)
                expectThat(gameThatWasSavedAndLoaded.id).isEqualTo(lobbyModifiedInMemoryOnly.id)
                expectThat(gameThatWasSavedAndLoaded.allEvents).isEqualTo(lobbyModifiedInMemoryOnly.allEvents)
                expectThat(gameThatWasSavedAndLoaded.state).isEqualTo(lobbyModifiedInMemoryOnly.state)
            }
        }

    @Test
    fun `a lobby that has not been saved cannot be loaded`() {
        expectThrows<LobbyDoesNotExist> { lobbyRepository.load(LobbyId.random()) }
    }
}
