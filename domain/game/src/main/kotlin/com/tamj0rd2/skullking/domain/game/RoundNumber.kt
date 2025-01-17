package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.between

// TODO: the round number can never be greater than 10
// TODO: Also, make a value to present, No Round or something
@JvmInline
value class RoundNumber private constructor(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<RoundNumber>(::RoundNumber, (1..10).between) {
        val none = RoundNumber(0)
    }

    fun next() = RoundNumber.of(value + 1)
}
