package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.Value

@JvmInline
value class PlayerId private constructor(override val value: String) : Value<String> {
    companion object : StringValueFactory<PlayerId>(::PlayerId)
}
