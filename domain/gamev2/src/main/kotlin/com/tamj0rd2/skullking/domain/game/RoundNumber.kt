package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value

data class RoundNumber private constructor(
    override val value: Int,
) : Value<Int>,
    Comparable<RoundNumber> {
    val next: RoundNumber get() = RoundNumber(value + 1)

    override fun compareTo(other: RoundNumber): Int = value.compareTo(other.value)

    companion object : IntValueFactory<RoundNumber>(::RoundNumber) {
        val none = RoundNumber(0)
        val finalRoundNumber: RoundNumber = RoundNumber.of(10)
    }
}
