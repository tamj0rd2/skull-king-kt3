package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.minValue

@JvmInline
value class Version private constructor(override val value: Int) : Value<Int>, Comparable<Version> {
    fun next(): Version = Version.of(value + 1)

    fun previous(): Version = Version.of(value - 1)

    operator fun plus(amount: Int) = Version.of(value + amount)

    override operator fun compareTo(other: Version) = value.compareTo(other.value)

    companion object : IntValueFactory<Version>(::Version, 0.minValue) {
        val NONE = Version(0)
        val INITIAL = NONE.next()
    }
}
