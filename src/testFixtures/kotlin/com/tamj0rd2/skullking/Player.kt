package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.application.Application
import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.application.ports.input.CreateGameInput
import com.tamj0rd2.skullking.application.ports.input.JoinGameInput
import com.tamj0rd2.skullking.application.ports.input.ViewGamesInput
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.testsupport.eventually
import java.util.concurrent.CopyOnWriteArrayList
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotEqualTo
import strikt.assertions.withSingle

class Player(val id: PlayerId, val application: Application) : ReceiveGameNotification {
    private val receivedNotifications = CopyOnWriteArrayList<GameNotification>()
    private val gameState
        get() = receivedNotifications.fold(GameState(), GameState::apply)

    fun `creates a game`() {
        application.createGameUseCase.execute(CreateGameInput(id))
    }

    fun `sees that the game has been created`() {
        val games = application.viewGamesUseCase.execute(ViewGamesInput).games
        expectThat(games).withSingle { get { host }.isEqualTo(id) }
    }

    fun `joins a game`() {
        val stateBeforeJoining = gameState

        val game = application.viewGamesUseCase.execute(ViewGamesInput).games.single()
        application.joinGameUseCase.execute(
            JoinGameInput(game.id, receiveGameNotification = this, playerId = id)
        )

        // todo: this output sucks. I actually want to try hamkrest again.
        eventually { expectThat(gameState).isNotEqualTo(stateBeforeJoining) }
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

    private data class GameState(val players: Set<PlayerId> = emptySet()) {
        fun apply(notification: GameNotification): GameState {
            return when (notification) {
                is GameNotification.PlayerJoined -> copy(players = players + notification.playerId)
            }
        }
    }
}
