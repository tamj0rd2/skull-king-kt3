package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.extensions.asFailure
import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteGame
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.GameIdMismatch
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.NotEnoughPlayersToCreateGame
import com.tamj0rd2.skullking.domain.gamev2.GameErrorCode.TooManyPlayersToCreateGame
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.CardPlayed
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.GameCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.RoundStarted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickCompleted
import com.tamj0rd2.skullking.domain.gamev2.GameEvent.TrickStarted
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.Objects
import java.util.UUID

data class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

typealias GameResult = Result4k<Game, GameErrorCode>

data class Game private constructor(
    val id: GameId,
    val state: GameState = GameState.new,
    val events: List<GameEvent> = emptyList(),
) {
    override fun equals(other: Any?): Boolean = other is Game && other.id == id

    override fun hashCode(): Int = Objects.hash(id)

    fun execute(command: GameCommand): GameResult =
        when (command) {
            is StartRound -> {
                appendEvent(
                    RoundStarted(
                        gameId = id,
                        roundNumber = command.roundNumber,
                        dealtCards = CardsPerPlayer(state.players.associateWith { setOf(CannedCard) }),
                    ),
                )
            }

            is PlaceABid ->
                appendEvent(
                    BidPlaced(
                        gameId = id,
                        bid = command.bid,
                        placedBy = command.actor,
                    ),
                )

            is StartTrick ->
                appendEvent(
                    TrickStarted(
                        gameId = id,
                        trickNumber = command.trickNumber,
                    ),
                )

            is PlayACard ->
                appendEvent(
                    CardPlayed(
                        gameId = id,
                        card = command.card,
                        playedBy = command.actor,
                    ),
                )

            is CompleteTrick ->
                appendEvent(
                    TrickCompleted(
                        gameId = id,
                        trickNumber = command.trickNumber,
                    ),
                )

            is CompleteRound ->
                appendEvent(
                    RoundCompleted(
                        gameId = id,
                        roundNumber = command.roundNumber,
                    ),
                )

            is CompleteGame ->
                appendEvent(
                    GameCompleted(
                        gameId = id,
                    ),
                )
        }

    private fun appendEvent(event: GameEvent): GameResult {
        if (event.gameId != id) return GameIdMismatch.asFailure()

        return copy(
            events = events + event,
            state = state.applyEvent(event).onFailure { return it },
        ).asSuccess()
    }

    companion object {
        const val MINIMUM_PLAYER_COUNT = 2
        const val MAXIMUM_PLAYER_COUNT = 6

        fun new(players: Set<PlayerId>): Result<Game, GameErrorCode> {
            if (players.size < MINIMUM_PLAYER_COUNT) return NotEnoughPlayersToCreateGame.asFailure()
            if (players.size > MAXIMUM_PLAYER_COUNT) return TooManyPlayersToCreateGame.asFailure()
            val id = GameId.random()
            return Game(id).appendEvent(GameStarted(gameId = id, players = players))
        }

        fun reconstituteFrom(events: List<GameEvent>): Result<Game, GameErrorCode> {
            val initial = Game(events.first().gameId).asSuccess() as Result<Game, GameErrorCode>
            return events.fold(initial) { result, command ->
                result.flatMap { it.appendEvent(command) }
            }
        }
    }
}
