package com.tamj0rd2.skullking.domain.game

enum class Bid(private val value: Int) {
    Zero(0),
    One(1),
    Two(2),
    Three(3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
    Eight(8),
    Nine(9),
    Ten(10);

    fun toInt(): Int = value

    companion object {
        private val reverseMapping = entries.associateBy { it.value }

        fun fromInt(value: Int): Bid {
            return reverseMapping.getValue(value)
        }

        val min = Zero
        val max = Ten
    }
}
