package com.tamj0rd2.skullking.domain.game

sealed interface Card

data object CannedCard : Card

data class CardsPerPlayer(
    val perPlayer: Map<PlayerId, Set<Card>>,
)
