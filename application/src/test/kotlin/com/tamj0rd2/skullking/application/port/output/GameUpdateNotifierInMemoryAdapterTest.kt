package com.tamj0rd2.skullking.application.port.output

class GameUpdateNotifierInMemoryAdapterTest : GameUpdateNotifierContract() {
    override val sut: GameUpdateNotifier = GameUpdateNotifierInMemoryAdapter()
}
