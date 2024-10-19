package com.tamj0rd2.skullking.application.port.input.roles

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.auth.SessionId
import com.tamj0rd2.skullking.domain.model.game.Card
import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.GameUpdate
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.CardDealt
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.GameStarted
import com.tamj0rd2.skullking.domain.model.game.GameUpdate.PlayerJoined
import com.tamj0rd2.skullking.domain.model.game.RoundNumber
import com.tamj0rd2.testhelpers.eventually
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.ZERO
import dev.forkhandles.values.random
import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.isNotEqualTo

class PlayerRole(
    private val driver: SkullKingUseCases,
) : GameUpdateListener {
    var id = PlayerId.ZERO
        private set

    // NOTE: for now I'm assuming the client generates the sessionId. I can have the server do this instead. The user can make an http request
    // to create a session, which is returned to the user. Then the user can send that along with the request to connect to the websocket.
    // https://devcenter.heroku.com/articles/websocket-security#authentication-authorization
    // Essentially, in application terms, I need some kind of "command" to create and return a sessionId, before doing anything else.
    private val sessionId = SessionId.random()

    override fun toString(): String = "$sessionId ($id)"

    private var gameId = GameId.NONE

    private val receivedGameUpdates = mutableListOf<GameUpdate>()

    fun createsAGame(): GameId = driver(CreateNewGameCommand(sessionId)).gameId

    fun joinsAGame(gameId: GameId): PlayerId {
        this.gameId = gameId
        val command =
            JoinGameCommand(
                sessionId = sessionId,
                gameId = gameId,
                gameUpdateListener = this,
            )
        return driver.invoke(command).orThrow().playerId.also {
            expectThat(it).isNotEqualTo(PlayerId.ZERO)
            id = it
        }
    }

    fun startsTheGame() {
        driver(StartGameCommand(gameId, id)).orThrow()
    }

    private var state = PlayerGameState()

    fun gameState(assertion: Builder<PlayerGameState>.() -> Unit) {
        eventually {
            expectThat(this).get { state }.assertion()
        }
    }

    override fun send(updates: List<GameUpdate>) {
        receivedGameUpdates += updates

        updates.forEach {
            state =
                state.run {
                    when (it) {
                        is CardDealt -> copy(hand = hand + it.card)
                        is GameStarted -> copy(roundNumber = roundNumber.next())
                        is PlayerJoined -> copy(players = players + it.playerId)
                    }
                }
        }
    }

    data class PlayerGameState(
        val roundNumber: RoundNumber = RoundNumber.none,
        val hand: List<Card> = emptyList(),
        val players: List<PlayerId> = emptyList(),
    ) {
        companion object {
            val Builder<PlayerGameState>.roundNumber get() = get { roundNumber }
            val Builder<PlayerGameState>.hand get() = get { hand }
            val Builder<PlayerGameState>.players get() = get { players }
        }
    }

    companion object {
        private var playerNumber = 0

        fun shortPlayerId() = ++playerNumber
    }
}
