package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propertyTest
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isIn

class Invariants {
    @Test
    fun `a valid game always has 2-6 players`() =
        propertyTest {
            Arb.game.validOnly().checkAll { game ->
                expectThat(game.state.players.size).isIn(2..6)
            }
        }
}
