package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.application.port.input.testsupport.each
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.Card
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

interface PlayACardUseCaseContract : UseCaseContract {

    @Test
    @Disabled
    fun `can play a card once all bids have been placed`() {
        val player1 = scenario.newPlayer()
        val player2 = scenario.newPlayer()
        val thePlayers = listOf(player1, player2)

        Given {
            player1.`creates a game`()
            player1.invites(player2)
            player2.`accepts the game invite`()
            player1.`starts the game`()

            player1.`places a bid`(Bid.of(0))
            player2.`places a bid`(Bid.of(0))
        }

        When {
            player1.`plays a card`(Card)
        }

        Then {
            thePlayers.each { `see a card`(Card, playedBy = player1.id) }
        }
    }
}
