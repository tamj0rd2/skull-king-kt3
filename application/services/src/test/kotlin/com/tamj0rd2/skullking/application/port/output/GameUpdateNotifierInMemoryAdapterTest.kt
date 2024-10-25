package com.tamj0rd2.skullking.application.port.output

import com.tamj0rd2.skullking.adapter.GameUpdateNotifierInMemoryAdapter

class GameUpdateNotifierInMemoryAdapterTest : GameUpdateNotifierContract() {
    override val sut: GameUpdateNotifier = GameUpdateNotifierInMemoryAdapter()
}
