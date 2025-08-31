package com.tamj0rd2.skullking.application.ports.output

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value
import dev.forkhandles.values.minValue

data class Version private constructor(override val value: Int) : Value<Int> {
    companion object : IntValueFactory<Version>(::Version, 0.minValue) {
        val initial = of(0)
    }
}

data class VersionedAtLoad<T> private constructor(val aggregate: T, val version: Version) {

    companion object {
        fun <T> VersionedAtLoad<T>.map(transform: (T) -> T): VersionedAtLoad<T> {
            return copy(aggregate = transform(aggregate))
        }

        fun <T> T.withLoadedVersion(version: Version): VersionedAtLoad<T> {
            return VersionedAtLoad(this, version)
        }
    }
}
