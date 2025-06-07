package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.adapter.inmemory.EventStoreInMemoryAdapter
import com.tamj0rd2.skullking.application.SkullKingApplication.OutputPorts

fun SkullKingApplication.Companion.usingTestDoublesByDefault(): SkullKingApplication =
    constructFromPorts(OutputPorts.usingTestDoublesByDefault())

fun OutputPorts.Companion.usingTestDoublesByDefault(): OutputPorts =
    OutputPorts(lobbyEventStore = EventStoreInMemoryAdapter())
