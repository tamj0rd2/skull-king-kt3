package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.application.port.inandout.LobbyNotification
import com.tamj0rd2.skullking.application.port.inandout.LobbyNotificationListener
import com.tamj0rd2.skullking.domain.game.LobbyId

interface LobbyNotifier {
    fun subscribe(
        lobbyId: LobbyId,
        listener: LobbyNotificationListener,
    )

    fun broadcast(
        lobbyId: LobbyId,
        updates: List<LobbyNotification>,
    )

    fun broadcast(
        lobbyId: LobbyId,
        vararg updates: LobbyNotification,
    ) {
        require(updates.isNotEmpty()) { "list of updates was empty" }
        return broadcast(lobbyId, updates.toList())
    }
}
