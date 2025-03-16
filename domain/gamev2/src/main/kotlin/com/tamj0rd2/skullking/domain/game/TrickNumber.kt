package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value

data class TrickNumber private constructor(
    override val value: Int,
) : Value<Int> {
    val next: TrickNumber get() = TrickNumber(value + 1)

    companion object : IntValueFactory<TrickNumber>(::TrickNumber) {
        val none = TrickNumber(0)
    }
}
