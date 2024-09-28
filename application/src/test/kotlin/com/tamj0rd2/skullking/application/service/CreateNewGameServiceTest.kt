package com.tamj0rd2.skullking.application.service

import com.tamj0rd2.skullking.application.ApplicationDomainDriver
import com.tamj0rd2.skullking.application.port.input.CreateNewGameGameUseCaseContract
import com.tamj0rd2.skullking.application.port.input.roles.PlayerRole
import com.tamj0rd2.skullking.application.usingTestDoublesByDefault

class CreateNewGameServiceTest : CreateNewGameGameUseCaseContract() {
    private val driver = ApplicationDomainDriver.usingTestDoublesByDefault()

    override fun newPlayerRole() = PlayerRole(driver)
}
