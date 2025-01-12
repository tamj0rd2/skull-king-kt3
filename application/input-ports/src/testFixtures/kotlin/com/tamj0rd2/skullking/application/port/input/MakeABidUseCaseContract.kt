package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlacedBid.UnknownBid
import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.domain.game.Bid
import org.junit.jupiter.api.Test

interface MakeABidUseCaseContract : UseCaseContract {
    @Test
    fun `when a player bids, other players can see that they have bid, but not what it is`() {
        val gameCreator = scenario.newPlayer()
        val anotherPlayer = scenario.newPlayer()

        Given {
            gameCreator.`creates a game`()
            gameCreator.invites(anotherPlayer)
            anotherPlayer.`accept the game invite`()
            gameCreator.`starts the game`()
        }

        When {
            gameCreator.`makes a bid`(Bid(1))
        }

        Then {
            anotherPlayer.`sees bid`(UnknownBid(madeBy = gameCreator.id))
        }
    }
}
