package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId

fun interface ViewPlayerGameStateUseCase {
    operator fun invoke(query: ViewPlayerGameStateQuery): ViewPlayerGameStateOutput
}

data class ViewPlayerGameStateQuery(
    val gameId: GameId,
    val playerId: PlayerId,
)

data class ViewPlayerGameStateOutput(
    val players: List<PlayerId>,
) {
    companion object {
        val empty get() = ViewPlayerGameStateOutput(
            players = emptyList(),
        )
    }
}
