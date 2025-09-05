package com.tamj0rd2.skullking.domain

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.minValue

data class Version private constructor(override val value: Int) : Value<Int> {
    companion object : IntValueFactory<Version>(::Version, 0.minValue) {
        val initial = of(0)
    }
}
