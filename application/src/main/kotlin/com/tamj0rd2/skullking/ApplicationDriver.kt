package com.tamj0rd2.skullking

import com.tamj0rd2.skullking.application.port.input.CreateNewGameUseCase
import com.tamj0rd2.skullking.application.port.input.JoinGameUseCase
import com.tamj0rd2.skullking.application.port.input.ViewPlayerGameStateUseCase

interface ApplicationDriver :
    CreateNewGameUseCase,
    JoinGameUseCase,
    ViewPlayerGameStateUseCase
