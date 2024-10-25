package com.tamj0rd2.skullking.adapter

import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifier
import com.tamj0rd2.skullking.application.port.output.GameUpdateNotifierContract

class GameUpdateNotifierInMemoryAdapterTest : GameUpdateNotifierContract() {
    override val sut: GameUpdateNotifier = GameUpdateNotifierInMemoryAdapter()
}
