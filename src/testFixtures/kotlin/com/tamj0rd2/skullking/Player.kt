package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.application.UseCases
import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import java.util.concurrent.CopyOnWriteArrayList
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.withSingle

class Player(val id: PlayerId, val useCases: UseCases) : ReceiveGameNotification {
    private val receivedNotifications = CopyOnWriteArrayList<GameNotification>()
    private val gameState
        get() = receivedNotifications.fold(GameState(), GameState::apply)

    fun `creates a game`() {
        useCases.createGameUseCase.execute(CreateGameInput(id))
    }

    fun `sees that the game has been created`() {
        val games = useCases.viewGamesUseCase.execute(ViewGamesInput).games
        expectThat(games).withSingle { get { host }.isEqualTo(id) }
    }

    fun `joins a game`() {
        val game = useCases.viewGamesUseCase.execute(ViewGamesInput).games.single()
        useCases.joinGameUseCase.execute(
            JoinGameInput(game.id, receiveGameNotification = this, playerId = id)
        )

        // todo: this output sucks. I actually want to try hamkrest again.
        eventually { expectThat(gameState.players).contains(id) }
    }

    fun `sees players in the game`(vararg expectedPlayers: Player) {
        expectThat(gameState)
            .get { players }
            .isNotEmpty()
            .containsExactlyInAnyOrder(expectedPlayers.map { it.id })
    }

    override fun receive(gameNotification: GameNotification) {
        receivedNotifications.add(gameNotification)
    }

    private data class GameState(val players: List<PlayerId> = emptyList()) {
        fun apply(notification: GameNotification): GameState {
            return when (notification) {
                is GameNotification.PlayerJoined -> copy(players = players + notification.playerId)
            }
        }
    }
}
