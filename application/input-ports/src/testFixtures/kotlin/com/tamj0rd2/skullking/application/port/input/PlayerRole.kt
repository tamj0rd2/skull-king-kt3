package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinAGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.auth.SessionId
import com.tamj0rd2.skullking.domain.game.Card
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.GameUpdate
import com.tamj0rd2.skullking.domain.game.GameUpdate.CardDealt
import com.tamj0rd2.skullking.domain.game.GameUpdate.GameStarted
import com.tamj0rd2.skullking.domain.game.GameUpdate.PlayerJoined
import com.tamj0rd2.skullking.domain.game.PlayerId
import com.tamj0rd2.skullking.domain.game.RoundNumber
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.isNotEqualTo
import java.time.Instant

class PlayerRole(
    private val driver: SkullKingUseCases,
) : GameUpdateListener {
    var id = PlayerId.NONE
        private set
        get() {
            expectThat(field).isNotEqualTo(PlayerId.NONE)
            return field
        }

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
            expectThat(it).isNotEqualTo(PlayerId.NONE)
            id = it
        }
    }

    fun startsTheGame() {
        driver(StartGameCommand(gameId, id)).orThrow()
    }

    private var state = PlayerGameState()

    fun hasGameStateWhere(assertion: Builder<PlayerGameState>.() -> Unit) {
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
        private fun <T> eventually(block: () -> T): T {
            val stopAt = Instant.now().plusMillis(200)
            var lastError: AssertionError?
            do {
                try {
                    return block()
                } catch (e: AssertionError) {
                    lastError = e
                }
            } while (stopAt > Instant.now())

            throw checkNotNull(lastError)
        }
    }
}
