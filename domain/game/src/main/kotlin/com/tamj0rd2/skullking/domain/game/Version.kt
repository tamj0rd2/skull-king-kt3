package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.minValue

@JvmInline
value class Version private constructor(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<Version>(::Version, 0.minValue) {
        val NONE = Version(0)
    }
}
