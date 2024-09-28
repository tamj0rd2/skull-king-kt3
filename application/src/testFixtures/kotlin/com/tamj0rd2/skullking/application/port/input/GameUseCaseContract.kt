package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole

interface GameUseCaseContract {
    fun newPlayerRole(): PlayerRole
}
