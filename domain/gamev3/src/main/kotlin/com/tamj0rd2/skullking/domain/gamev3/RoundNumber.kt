package com.tamj0rd2.skullking.domain.gamev3

enum class RoundNumber {
    One,
    Two,
    Three,
    Four,
    Five,
    Six,
    Seven,
    Eight,
    Nine,
    Ten;

    fun next(): RoundNumber =
        when (this) {
            One -> Two
            Two -> Three
            Three -> Four
            Four -> Five
            Five -> Six
            Six -> Seven
            Seven -> Eight
            Eight -> Nine
            Nine -> Ten
            Ten -> throw IllegalStateException("Cannot advance past the last round")
        }
}
