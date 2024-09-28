package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.application.port.input.InMemoryEventListeners
import com.tamj0rd2.skullking.domain.model.*

class GameRepositoryInMemoryAdapter(
    private val listeners: InMemoryEventListeners = InMemoryEventListeners(),
) : GameRepository {
    private val savedEvents = mutableMapOf<GameId, List<GameEvent>>()

    override fun load(gameId: GameId): Game = Game.from(savedEvents.getOrDefault(gameId, emptyList()))

    override fun save(game: Game) {
        savedEvents[game.id] = game.events

        // TODO: something else should be converting the events into notifications
        listeners.broadcast(
            game.newEvents.mapNotNull {
                when (it) {
                    is GameCreated -> null
                    is PlayerJoined -> GameUpdate.PlayerJoined(it.playerId)
                }
            },
        )
    }
}
