package com.tamj0rd2.skullking.domain.game.values

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.between

data class TrickNumber private constructor(
    override val value: Int,
) : Value<Int> {
    val next: TrickNumber get() = TrickNumber(value + 1)

    companion object : IntValueFactory<TrickNumber>(::TrickNumber, (1..10).between) {
        val none = TrickNumber(0)
        val first = TrickNumber.of(1)
        val last = TrickNumber.of(10)
    }
}
