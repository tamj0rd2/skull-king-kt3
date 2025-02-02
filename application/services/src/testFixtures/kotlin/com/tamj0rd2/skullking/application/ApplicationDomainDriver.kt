package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.adapter.inmemory.EventStoreInMemoryAdapter
import com.tamj0rd2.skullking.adapter.inmemory.PlayerIdStorageInMemoryAdapter
import com.tamj0rd2.skullking.application.SkullKingApplication.OutputPorts

fun SkullKingApplication.Companion.usingTestDoublesByDefault(): SkullKingApplication {
    val playerIdStorage = PlayerIdStorageInMemoryAdapter()

    return constructFromPorts(
        outputPorts =
            OutputPorts(
                lobbyEventStore = EventStoreInMemoryAdapter(),
                findPlayerIdPort = playerIdStorage,
                savePlayerIdPort = playerIdStorage,
            ),
    )
}
