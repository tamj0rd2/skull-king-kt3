package com.tamj0rd2.skullking.application.ports.output

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.minValue

data class LoadedVersion private constructor(override val value: Int) : Value<Int> {
    companion object : IntValueFactory<LoadedVersion>(::LoadedVersion, 0.minValue) {
        val initial = of(0)
    }
}
