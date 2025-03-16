package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value

data class RoundNumber private constructor(
    override val value: Int,
) : Value<Int> {
    val next: RoundNumber get() = RoundNumber(value + 1)

    companion object : IntValueFactory<RoundNumber>(::RoundNumber) {
        val none = RoundNumber(0)
    }
}
