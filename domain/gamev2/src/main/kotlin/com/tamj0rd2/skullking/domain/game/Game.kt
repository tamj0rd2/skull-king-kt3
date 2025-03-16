package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.extensions.asSuccess
import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameEvent.BidPlaced
import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import com.tamj0rd2.skullking.domain.game.GameEvent.RoundStarted
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.UUIDValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.random
import java.util.UUID

data class GameId private constructor(
    override val value: UUID,
) : Value<UUID> {
    companion object : UUIDValueFactory<GameId>(::GameId)
}

class Game private constructor(
    val id: GameId,
) {
    val events = mutableListOf<GameEvent>()

    constructor(players: Set<PlayerId>) : this(
        id = GameId.random(),
    ) {
        appendEvent(GameStarted(gameId = id, players = players)).orThrow()
    }

    var state = GameState.new
        private set

    fun execute(command: GameCommand): Result4k<Unit, GameErrorCode> =
        when (command) {
            is StartRound ->
                appendEvent(
                    RoundStarted(
                        gameId = id,
                        roundNumber = state.roundNumber.next,
                        dealtCards = CardsPerPlayer(state.players.associateWith { setOf(CannedCard) }),
                    ),
                )

            is PlaceABid ->
                appendEvent(
                    BidPlaced(
                        gameId = id,
                        bid = command.bid,
                        placedBy = command.actor,
                    ),
                )
        }

    private fun appendEvent(event: GameEvent): Result4k<Unit, GameErrorCode> {
        check(event.gameId == id) { "GameId mismatch" }
        state = state.applyEvent(event).onFailure { return it }
        events.add(event)
        return Unit.asSuccess()
    }
}
