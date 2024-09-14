package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId

fun interface ViewPlayerGameStateUseCase {
    operator fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput

    data class ViewPlayerGameStateQuery(
        val gameId: GameId,
        val playerId: PlayerId,
    )

    data class ViewPlayerGameStateOutput(
        val players: List<PlayerId>,
    )
}
