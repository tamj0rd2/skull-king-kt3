package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.application.port.input.testsupport.each
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.Card
import org.junit.jupiter.api.Test

interface PlayACardUseCaseContract : UseCaseContract {
    val propertyTestIterations: Int get() = 1000

    @Test
    fun `can play a card once all bids have been placed`() {
        val player1 = scenario.newPlayer()
        val player2 = scenario.newPlayer()
        val thePlayers = listOf(player1, player2)

        Given {
            player1.`creates a lobby`()
            player1.invites(player2)
            player2.`accepts the lobby invite`()
            player1.`starts the game`()

            player1.`places a bid`(Bid.of(0))
            player2.`places a bid`(Bid.of(0))
        }

        When {
            player1.`plays a card in their hand`()
        }

        Then {
            thePlayers.each { `see a card`(Card, playedBy = player1.id) }
        }
    }

    @Test
    fun `when all players have played their cards, the winner of the trick is determined`() {
        val player1 = scenario.newPlayer()
        val player2 = scenario.newPlayer()
        val thePlayers = listOf(player1, player2)

        Given {
            player1.`creates a lobby`()
            player1.invites(player2)
            player2.`accepts the lobby invite`()
            player1.`starts the game`()

            player1.`places a bid`(Bid.of(0))
            player2.`places a bid`(Bid.of(0))
        }

        When {
            thePlayers.each { `play a card in their hand`() }
        }

        Then {
            thePlayers.each { `see that the trick winner has been chosen`() }
        }
    }

    // TODO: business rule - when a card is played, that player's hand size decreases by 1.
    // TODO: each player can only play 1 card per trick.
    // TODO: define rules about how the trick winner is chosen
}
