package com.tamj0rd2.skullking.adapter.web

import com.tamj0rd2.skullking.adapter.web.MessageFromClient.PlaceABidMessage
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase
import com.tamj0rd2.skullking.application.port.input.PlaceABidUseCase.PlaceABidCommand
import dev.forkhandles.result4k.orThrow

internal class PlaceABidController(
    private val placeABidUseCase: PlaceABidUseCase,
) : MessageReceiver<PlaceABidMessage> {
    override operator fun invoke(
        session: PlayerSession,
        message: PlaceABidMessage,
    ) {
        val command = PlaceABidCommand(session.lobbyId, session.playerId, message.bid)
        placeABidUseCase.invoke(command).orThrow()
    }
}
