package com.tamj0rd2.skullking.domain.gamev2.values

enum class RoundNumber(
    private val value: Int,
    val next: () -> RoundNumber,
) {
    One(value = 1, next = { Two }),
    Two(value = 2, next = { Three }),
    Three(value = 3, next = { Four }),
    Four(value = 4, next = { Five }),
    Five(value = 5, next = { Six }),
    Six(value = 6, next = { Seven }),
    Seven(value = 7, next = { Eight }),
    Eight(value = 8, next = { Nine }),
    Nine(value = 9, next = { Ten }),
    Ten(value = 10, next = { error("${Ten::class.simpleName} is the final round.") }),
    ;

    val totalCardsToDeal = value

    fun differenceFrom(other: RoundNumber): Int = this.value - other.value
}
