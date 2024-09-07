package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase

interface ApplicationDriver :
    ViewPlayerGameStateUseCase,
    JoinGameUseCase
