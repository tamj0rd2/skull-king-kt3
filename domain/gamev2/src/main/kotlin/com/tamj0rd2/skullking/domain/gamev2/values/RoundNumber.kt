package com.tamj0rd2.skullking.domain.gamev2.values

import dev.forkhandles.values.Value

// TODO: wait... this is just an enum. Wtf am I doing?
data class RoundNumber private constructor(
    override val value: Int,
) : Value<Int>,
    Comparable<RoundNumber> {
    val next: RoundNumber get() = RoundNumber(value + 1)

    override fun compareTo(other: RoundNumber): Int = value.compareTo(other.value)

    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        val one = RoundNumber(1)
        val two = RoundNumber(2)
        val three = RoundNumber(3)
        val four = RoundNumber(4)
        val five = RoundNumber(5)
        val six = RoundNumber(6)
        val seven = RoundNumber(7)
        val eight = RoundNumber(8)
        val nine = RoundNumber(9)
        val ten = RoundNumber(10)

        val all = setOf(one, two, three, four, five, six, seven, eight, nine, ten)
    }
}
