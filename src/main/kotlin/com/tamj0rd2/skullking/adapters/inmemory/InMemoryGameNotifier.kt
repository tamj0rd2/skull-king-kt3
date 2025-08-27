package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.GameNotifier
import com.tamj0rd2.skullking.application.ports.PlayerSpecificGameState
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification
import com.tamj0rd2.skullking.domain.game.PlayerId

class InMemoryGameNotifier : GameNotifier {
    private val subscribers = mutableMapOf<PlayerId, ReceiveGameNotification>()

    override fun subscribe(playerId: PlayerId, receiver: ReceiveGameNotification) {
        subscribers[playerId] = receiver
    }

    override fun send(recipient: PlayerId, playerSpecificGameState: PlayerSpecificGameState) {
        subscribers.getValue(recipient).receive(playerSpecificGameState)
    }
}
