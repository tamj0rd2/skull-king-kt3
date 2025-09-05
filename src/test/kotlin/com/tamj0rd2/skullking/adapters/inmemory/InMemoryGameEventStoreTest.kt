package com.tamj0rd2.skullking.adapters.inmemory

import com.tamj0rd2.skullking.application.ports.output.GameEventStore
import com.tamj0rd2.skullking.application.ports.output.GameEventStoreContract

class InMemoryGameEventStoreTest : GameEventStoreContract {
    override val eventStore: GameEventStore = InMemoryGameEventStore()
}
