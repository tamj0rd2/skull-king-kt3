package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

interface PlaceABidUseCase {
    data class PlaceABidCommand(
        val lobbyId: LobbyId,
        val playerId: PlayerId,
        val bid: Bid,
    )

    data object PlaceABidOutput

    operator fun invoke(command: PlaceABidCommand): Result4k<PlaceABidOutput, LobbyErrorCode>
}
