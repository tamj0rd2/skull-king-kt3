package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.minValue

@JvmInline
value class LoadedGameVersion private constructor(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<LoadedGameVersion>(::LoadedGameVersion, 0.minValue) {
        val NONE = LoadedGameVersion(-1)
    }
}
