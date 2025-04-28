package com.tamj0rd2.skullking.domain.gamev2.values

import dev.forkhandles.values.LongValueFactory
import dev.forkhandles.values.Value

data class Version private constructor(
    override val value: Long,
) : Value<Long> {
    companion object : LongValueFactory<Version>(::Version) {
        val none = Version(0)
    }
}
