package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.GameArbs.gameIdArb
import com.tamj0rd2.skullking.domain.PlayerArbs.playerIdArb
import com.tamj0rd2.skullking.domain.model.Game.Companion.MAXIMUM_PLAYER_COUNT
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
            val playerJoinedEvents by playerJoinedArb().list().ofMaxSize(MAXIMUM_PLAYER_COUNT)

            combineAs {
                listOf(
                    GameCreated(gameId = gameId),
                ) + playerJoinedEvents.map { it.withGameId(gameId) }
            }
        }

//    @Provide
//    fun gameEventArb() = anyForSubtypeOf<GameEvent> {
//        provide<GameCreated> { gameIdArb().map { gameId -> GameCreated(gameId) } }
//        provide<PlayerJoined> { gameIdArb().map { gameId -> playerJoinedArb(gameId) } }
//    }

    fun gameCreatedArb(gameId: GameId): GameCreated = GameCreated(gameId = gameId)

    fun playerJoinedArb(): Arbitrary<PlayerJoined> =
        combine {
            val gameId by gameIdArb()
            val playerId by playerIdArb()

            combineAs {
                PlayerJoined(gameId = gameId, playerId = playerId)
            }
        }
}

fun GameEvent.withGameId(gameId: GameId) =
    when (this) {
        is GameCreated -> copy(gameId = gameId)
        is PlayerJoined -> copy(gameId = gameId)
    }
