package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import com.tamj0rd2.skullking.domain.game.LobbyId
import com.tamj0rd2.skullking.domain.game.PlayerId
import dev.forkhandles.result4k.orThrow

internal class PlaceABidController(
    private val placeABidUseCase: PlaceABidUseCase,
) : MessageReceiver<PlaceABidMessage> {
    override operator fun invoke(
        ws: MessageSender,
        playerId: PlayerId,
        lobbyId: LobbyId,
        message: PlaceABidMessage,
    ) {
        val command = PlaceABidCommand(lobbyId, playerId, message.bid)
        placeABidUseCase.invoke(command).orThrow()
    }
}
