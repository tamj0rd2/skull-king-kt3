package com.tamj0rd2.skullking.application

import com.tamj0rd2.skullking.application.ports.output.LoadAllGamesPort
import com.tamj0rd2.skullking.application.ports.output.SaveGamePort

data class OutputPorts(val saveGamePort: SaveGamePort, val loadAllGamesPort: LoadAllGamesPort)
