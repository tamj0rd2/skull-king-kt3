package com.tamj0rd2.skullking.domain.gamev2.values

import dev.forkhandles.values.Value

data class Bid private constructor(
    override val value: Int,
) : Value<Int> {
    @Suppress("MemberVisibilityCanBePrivate")
    companion object {
        val zero = Bid(0)
        val one = Bid(1)
        val two = Bid(2)
        val three = Bid(3)
        val four = Bid(4)
        val five = Bid(5)
        val six = Bid(6)
        val seven = Bid(7)
        val eight = Bid(8)
        val nine = Bid(9)
        val ten = Bid(10)

        val allPossibleBids = setOf(zero, one, two, three, four, five, six, seven, eight, nine, ten)
    }
}
