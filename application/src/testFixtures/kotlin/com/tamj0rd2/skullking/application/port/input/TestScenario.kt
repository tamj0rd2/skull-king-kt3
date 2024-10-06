package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole

interface TestScenario {
    fun newPlayer(): PlayerRole
}
