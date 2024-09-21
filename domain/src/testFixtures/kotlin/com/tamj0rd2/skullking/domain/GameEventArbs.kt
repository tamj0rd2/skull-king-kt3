package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.GameArbs.gameIdArb
import com.tamj0rd2.skullking.domain.PlayerArbs.playerIdArb
import com.tamj0rd2.skullking.domain.PlayerArbs.playerIdsArb
import com.tamj0rd2.skullking.domain.model.GameCreated
import com.tamj0rd2.skullking.domain.model.GameEvent
import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import net.jqwik.api.Arbitrary
import net.jqwik.api.Provide
import net.jqwik.api.domains.DomainContextBase
import net.jqwik.kotlin.api.combine

@Suppress("unused")
object GameEventArbs : DomainContextBase() {
    @Provide
    fun gameEventsForASingleGameArb(): Arbitrary<List<GameEvent>> =
        combine {
            val gameId by gameIdArb()
            val playerIds by playerIdsArb()

            combineAs {
                val playerJoinedEvents = playerIds.map { playerId -> PlayerJoined(gameId = gameId, playerId = playerId) }
                listOf(GameCreated(gameId = gameId)) + playerJoinedEvents
            }
        }

    fun gameCreatedArb(gameId: GameId): GameCreated = GameCreated(gameId = gameId)

    private fun playerJoinedArb(): Arbitrary<PlayerJoined> =
        combine {
            val gameId by gameIdArb()
            val playerId by playerIdArb()

            combineAs {
                PlayerJoined(gameId = gameId, playerId = playerId)
            }
        }
}
