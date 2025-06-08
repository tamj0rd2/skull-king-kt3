package com.tamj0rd2.skullking.domain.gamev3

sealed interface Card

data class NumberedCard(val suit: Suit, val value: CardValue) : Card {
    enum class Suit {
        Red,
        Yellow,
        Blue,
        Black,
    }

    enum class CardValue {
        One,
        Two,
        Three,
        Four,
        Five,
        Six,
        Seven,
        Eight,
        Nine,
        Ten,
        Eleven,
        Twelve,
        Thirteen,
    }
}

data class PlayedCard(val card: Card, val playerId: PlayerId) {
    companion object {
        fun Card.playedBy(playerId: PlayerId): PlayedCard =
            PlayedCard(card = this, playerId = playerId)
    }
}
