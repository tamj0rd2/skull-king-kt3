package com.tamj0rd2.skullking.domain.gamev2

sealed interface Card

data object CannedCard : Card

data class CardsPerPlayer(
    val perPlayer: Map<PlayerId, Set<Card>>,
)
