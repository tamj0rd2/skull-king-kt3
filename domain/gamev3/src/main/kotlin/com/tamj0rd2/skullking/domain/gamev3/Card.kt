package com.tamj0rd2.skullking.domain.gamev3

sealed interface Card

data object CardA : Card

data object CardB : Card

data class PlayedCard(
    val card: Card,
    val playerId: PlayerId,
) {
    companion object {
        fun Card.playedBy(playerId: PlayerId): PlayedCard = PlayedCard(card = this, playerId = playerId)
    }
}
