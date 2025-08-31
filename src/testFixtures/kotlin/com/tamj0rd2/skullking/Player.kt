package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.UseCases
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.domain.game.PlayerId
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull

class Player(
    val id: PlayerId,
    val useCases: UseCases,
    val deriveGameState: DeriveGameState,
    val eventually: (block: () -> Unit) -> Unit,
) {
    private val gameState
        get() = checkNotNull(deriveGameState.current())

    fun `creates a game`() = expectingStateChange {
        useCases.createGameUseCase.execute(
            CreateGameInput(
                receiveGameNotification =
                    if (deriveGameState is ReceiveGameNotification) deriveGameState
                    else ReceiveGameNotification {},
                playerId = id,
            )
        )
    }

    fun `joins a game`() = expectingStateChange {
        val gameId = useCases.viewGamesUseCase.execute(ViewGamesInput).games.single().id
        useCases.joinGameUseCase.execute(
            JoinGameInput(
                gameId = gameId,
                receiveGameNotification =
                    if (deriveGameState is ReceiveGameNotification) deriveGameState
                    else ReceiveGameNotification {},
                playerId = id,
            )
        )
    }

    fun `sees players in the game`(vararg expectedPlayers: Player) {
        expectThat(gameState)
            .get { players }
            .isNotEmpty()
            .containsExactlyInAnyOrder(expectedPlayers.map { it.id })
    }

    interface DeriveGameState {
        fun current(): PlayerSpecificGameState?
    }

    private fun expectingStateChange(block: () -> Unit) {
        val stateBefore = deriveGameState.current()
        block()

        // todo: this output sucks. I actually want to try hamkrest again.
        eventually { expectThat(deriveGameState.current()).isNotNull().isNotEqualTo(stateBefore) }
    }
}
