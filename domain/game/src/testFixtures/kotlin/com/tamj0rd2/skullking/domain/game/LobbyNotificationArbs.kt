package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.LobbyNotification.APlayerHasJoined
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.choice

object LobbyNotificationArbs {
    private val playerJoinedLobbyNotificationArb =
        arbitrary {
            val playerId = LobbyArbs.playerIdArb.bind()
            APlayerHasJoined(playerId)
        }

    val lobbyNotificationArb =
        Arb.choice(
            playerJoinedLobbyNotificationArb,
        )
}
