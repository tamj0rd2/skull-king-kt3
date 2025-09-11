package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.Version
import dev.forkhandles.values.random

data class Game
private constructor(
    val id: GameId,
    val creator: PlayerId,
    val events: List<GameEvent> = emptyList(),
    val players: Set<PlayerId> = emptySet(),
    val roundNumber: RoundNumber? = null,
    val placedBids: Map<PlayerId, Bid> = emptyMap(),
    val phase: GamePhase = GamePhase.WaitingForPlayers,
) {
    val version = Version.of(events.size)

    companion object {
        fun new(createdBy: PlayerId): Game {
            val id = GameId.random()
            return Game(id = id, creator = createdBy).applyEvent(GameEvent.GameCreated(gameId = id, createdBy = createdBy))
        }

        fun reconstitute(events: List<GameEvent>): Game {
            val creationEvent = events.first() as GameEvent.GameCreated
            return events.fold(Game(id = creationEvent.gameId, creator = creationEvent.createdBy), Game::applyEvent)
        }
    }

    fun execute(command: GameCommand): Game {
        return when (command) {
            is GameCommand.AddPlayer -> addPlayer(command.playerId)
            is GameCommand.StartGame -> start()
            is GameCommand.PlaceBid -> placeBid(command.playerId, command.bid)
        }
    }

    private fun addPlayer(playerId: PlayerId): Game {
        return applyEvent(GameEvent.PlayerJoined(gameId = id, playerId = playerId))
    }

    private fun start(): Game {
        return applyEvent(GameEvent.RoundStarted(gameId = id, roundNumber = RoundNumber.One))
    }

    private fun placeBid(playerId: PlayerId, bid: Bid): Game {
        return applyEvent(GameEvent.BidPlaced(gameId = id, playerId = playerId, bid = bid))
    }

    private fun applyEvent(event: GameEvent): Game {
        return when (event) {
            is GameEvent.GameCreated -> copy(creator = event.createdBy, players = setOf(event.createdBy))

            is GameEvent.PlayerJoined -> copy(players = players + event.playerId)

            is GameEvent.RoundStarted -> copy(roundNumber = event.roundNumber, phase = GamePhase.Bidding)

            is GameEvent.BidPlaced -> copy(placedBids = placedBids + (event.playerId to event.bid))
        }.copy(events = events + event)
    }
}

sealed interface GameCommand {
    data class AddPlayer(val playerId: PlayerId) : GameCommand

    data class PlaceBid(val playerId: PlayerId, val bid: Bid) : GameCommand

    object StartGame : GameCommand
}

enum class GamePhase {
    WaitingForPlayers,
    Bidding,
}
