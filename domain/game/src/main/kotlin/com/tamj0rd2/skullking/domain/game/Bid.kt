package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.Value

@JvmInline
value class Bid(
    override val value: Int,
) : Value<Int> {
    companion object : IntValueFactory<Bid>(::Bid)

    // TODO: a bit can only be from 0-10
}
