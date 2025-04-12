package com.tamj0rd2.skullking.domain.game.values

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.between

data class Bid private constructor(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<Bid>(::Bid, (0..10).between) {
        val absoluteMin = Bid.of(0)
        val absoluteMax = Bid.of(10)
    }
}
