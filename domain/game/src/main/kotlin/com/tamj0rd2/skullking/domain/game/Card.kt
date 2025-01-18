package com.tamj0rd2.skullking.domain.game

data object Card

data class PlayedCard(
    val card: Card,
    val playedBy: PlayerId,
)
