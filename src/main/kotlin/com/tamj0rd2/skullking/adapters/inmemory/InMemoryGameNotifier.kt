package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.GameNotification
import com.tamj0rd2.skullking.application.ports.GameNotifier
import com.tamj0rd2.skullking.application.ports.ReceiveGameNotification

class InMemoryGameNotifier : GameNotifier {
    private val subscribers = mutableListOf<ReceiveGameNotification>()
    private val notifications = mutableListOf<GameNotification>()

    override fun subscribe(receiver: ReceiveGameNotification) {
        subscribers.add(receiver)
        notifications.forEach { notification -> receiver.receive(notification) }
    }

    override fun send(gameNotification: GameNotification) {
        notifications.add(gameNotification)
        subscribers.forEach { subscriber -> subscriber.receive(gameNotification) }
    }
}
