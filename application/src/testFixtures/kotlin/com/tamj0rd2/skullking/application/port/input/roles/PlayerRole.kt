package com.tamj0rd2.skullking.application.port.input.roles

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase.CreateNewGameCommand
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase.JoinGameCommand
import com.tamj0rd2.skullking.application.port.input.SkullKingUseCases
import com.tamj0rd2.skullking.application.port.input.StartGameUseCase.StartGameCommand
import com.tamj0rd2.skullking.application.port.output.GameUpdateListener
import com.tamj0rd2.skullking.domain.model.PlayerId
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
import strikt.api.Assertion.Builder
import strikt.api.expectThat
import strikt.assertions.contains

class PlayerRole(
    private val driver: SkullKingUseCases,
) : GameUpdateListener {
    var id = PlayerId.ZERO
        private set

    private var gameId = GameId.NONE

    private val receivedGameUpdates = mutableListOf<GameUpdate>()

    fun createsAGame(): GameId = driver(CreateNewGameCommand).gameId

    fun joinsAGame(gameId: GameId): PlayerId {
        this.gameId = gameId
        val command = JoinGameCommand(gameId = gameId, gameUpdateListener = this)
        return driver(command).orThrow().playerId.also { id = it }
    }

    fun startsTheGame() {
        driver(StartGameCommand(gameId, id)).orThrow()
    }

    private var gameUpdatesRead = 0

    fun received(expectedUpdate: GameUpdate) {
        eventually {
            val recentGameUpdates = receivedGameUpdates.drop(gameUpdatesRead)
            expectThat(recentGameUpdates).contains(expectedUpdate)
            gameUpdatesRead = recentGameUpdates.indexOfFirst { it == expectedUpdate } + 1
        }
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
            when (it) {
                is CardDealt -> state = state.run { copy(hand = hand + it.card) }
                is GameStarted -> state = state.run { copy(roundNumber = roundNumber.next()) }
                is PlayerJoined -> Unit
            }
        }
    }

    data class PlayerGameState(
        val roundNumber: RoundNumber = RoundNumber.none,
        val hand: List<Card> = emptyList(),
    ) {
        companion object {
            val Builder<PlayerGameState>.roundNumber get() = get { roundNumber }.describedAs("roundNumber")
            val Builder<PlayerGameState>.hand get() = get { hand }.describedAs("hand")
        }
    }
}
