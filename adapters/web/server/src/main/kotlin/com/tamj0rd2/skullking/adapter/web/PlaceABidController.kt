package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.domain.game.LobbyErrorCode
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.Result4k

internal class PlaceABidController(
    private val placeABidUseCase: PlaceABidUseCase,
) : MessageReceiver<PlaceABidMessage> {
    override fun receive(
        playerId: PlayerId,
        lobbyId: LobbyId,
        message: PlaceABidMessage,
    ): Result4k<*, LobbyErrorCode> {
        val command = PlaceABidCommand(lobbyId, playerId, message.bid)
        return placeABidUseCase.invoke(command)
    }
}
