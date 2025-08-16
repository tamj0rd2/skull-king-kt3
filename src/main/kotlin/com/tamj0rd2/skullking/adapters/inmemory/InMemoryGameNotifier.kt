package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.GameNotifier
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification

class InMemoryGameNotifier : GameNotifier {
    private val subscribers = mutableListOf<ReceiveGameNotification>()

    override fun subscribe(receiveGameNotification: ReceiveGameNotification) {
        subscribers.add(receiveGameNotification)
    }

    override fun send(gameNotification: GameNotification) {
        subscribers.forEach { subscriber -> subscriber.receive(gameNotification) }
    }
}
