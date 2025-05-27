package com.tamj0rd2.skullking.domain.gamev3

sealed interface Bid

enum class SomeBid : Bid {
    Zero,
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
}

data object NoBid : Bid
