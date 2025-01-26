package com.tamj0rd2.skullking.adapter.inmemory

import com.tamj0rd2.skullking.application.port.output.LobbyNotifier
import com.tamj0rd2.skullking.application.port.output.LobbyUpdateNotifierContract

class LobbyUpdateNotifierInMemoryAdapterTest : LobbyUpdateNotifierContract() {
    override val sut: LobbyNotifier = LobbyNotifierInMemoryAdapter()
}
