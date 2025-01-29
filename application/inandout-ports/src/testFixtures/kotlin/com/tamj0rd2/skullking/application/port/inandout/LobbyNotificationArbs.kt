package com.tamj0rd2.skullking.application.port.inandout

import com.tamj0rd2.skullking.domain.game.LobbyArbs
import com.tamj0rd2.skullking.domain.game.LobbyNotification
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice

object LobbyNotificationArbs {
    private val playerJoinedLobbyNotificationArb =
        arbitrary {
            val playerId = LobbyArbs.playerIdArb.bind()
            LobbyNotification.APlayerHasJoined(playerId)
        }

    val lobbyNotificationArb =
        Arb.choice(
            playerJoinedLobbyNotificationArb,
        )
}
