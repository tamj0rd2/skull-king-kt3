package com.tamj0rd2.skullking.domain.game.values

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.between

data class RoundNumber private constructor(
    override val value: Int,
) : Value<Int>,
    Comparable<RoundNumber> {
    val next: RoundNumber get() = RoundNumber(value + 1)

    override fun compareTo(other: RoundNumber): Int = value.compareTo(other.value)

    companion object : IntValueFactory<RoundNumber>(::RoundNumber, (1..10).between) {
        val none = RoundNumber(0)
        val first = RoundNumber.of(1)
        val last: RoundNumber = RoundNumber.of(10)
    }
}
