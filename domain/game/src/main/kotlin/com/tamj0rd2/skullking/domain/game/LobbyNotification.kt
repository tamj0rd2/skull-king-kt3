package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.LobbyNotificationRecipient.Everyone
import com.tamj0rd2.skullking.domain.game.LobbyNotificationRecipient.Someone

// TODO: notifications should be _for_ someone.
sealed interface LobbyNotification {
    val recipient: LobbyNotificationRecipient

    data class APlayerHasJoined(val playerId: PlayerId) : LobbyNotification {
        override val recipient = Everyone
    }

    data object TheGameHasStarted : LobbyNotification {
        override val recipient = Everyone
    }

    data class CardsWereDealt(val cards: List<Card>, override val recipient: Someone) :
        LobbyNotification

    data class ABidWasPlaced(val playerId: PlayerId) : LobbyNotification {
        override val recipient = Everyone
    }

    data class AllBidsHaveBeenPlaced(val bids: Map<PlayerId, Bid>) : LobbyNotification {
        override val recipient = Everyone
    }

    data class ACardWasPlayed(val playedCard: PlayedCard) : LobbyNotification {
        override val recipient = Everyone
    }

    data class TheTrickHasEnded(val winner: PlayerId) : LobbyNotification {
        override val recipient = Everyone
    }
}

sealed class LobbyNotificationRecipient {
    data object Everyone : LobbyNotificationRecipient()

    data class Someone(val playerId: PlayerId) : LobbyNotificationRecipient()
}
