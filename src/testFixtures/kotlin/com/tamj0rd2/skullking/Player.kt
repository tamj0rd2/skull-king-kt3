package com.tamj0rd2.skullking

import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.isEmpty
import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.StartGameInput
import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.RoundNumber

class Player(val id: PlayerId, val useCases: UseCases, val deriveGameState: DeriveGameState, val eventually: (block: () -> Unit) -> Unit) {
    private val gameState: PlayerSpecificGameState
        get() = checkNotNull(deriveGameState.current())

    fun `creates a game`() = expectingStateChange {
        useCases.createGameUseCase.execute(
            CreateGameInput(
                receiveGameNotification = if (deriveGameState is ReceiveGameNotification) deriveGameState else ReceiveGameNotification {},
                playerId = id,
            )
        )
    }

    fun `joins a game`() = expectingStateChange {
        val gameId = useCases.viewGamesUseCase.execute(ViewGamesInput).games.single().id
        useCases.joinGameUseCase.execute(
            JoinGameInput(
                gameId = gameId,
                receiveGameNotification = if (deriveGameState is ReceiveGameNotification) deriveGameState else ReceiveGameNotification {},
                playerId = id,
            )
        )
    }

    fun `sees players in the game`(vararg expectedPlayers: Player) {
        assertThat(gameState, has(PlayerSpecificGameState::players, !isEmpty and equalTo(expectedPlayers.map { it.id })))
    }

    fun `starts the game`() = expectingStateChange { useCases.startGameUseCase.execute(StartGameInput(gameState.gameId)) }

    fun `sees the round number`(expected: RoundNumber) {
        assertThat(gameState, has(PlayerSpecificGameState::roundNumber, !isNull and equalTo(expected)))
    }

    interface DeriveGameState {
        fun current(): PlayerSpecificGameState?
    }

    private fun expectingStateChange(block: () -> Unit) {
        val stateBefore = deriveGameState.current()
        block()
        eventually {
            assertThat(deriveGameState.current(), !isNull and !equalTo(stateBefore)) {
                "expected the state to change after performing the action"
            }
        }
    }
}
