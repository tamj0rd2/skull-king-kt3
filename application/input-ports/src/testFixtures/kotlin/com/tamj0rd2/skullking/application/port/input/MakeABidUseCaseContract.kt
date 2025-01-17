package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.PlacedBid.RevealedBid.Companion.madeBy
import com.tamj0rd2.skullking.application.port.input.PlacedBid.UnknownBid
import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.application.port.input.testsupport.each
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GameArbs.validBid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import org.junit.jupiter.api.Test

interface MakeABidUseCaseContract : UseCaseContract {
    @Test
    fun `when a player bids, other players can see that they have bid, but not what it is`() {
        val gameCreator = scenario.newPlayer()
        val anotherPlayer = scenario.newPlayer()

        Given {
            gameCreator.`creates a game`()
            gameCreator.invites(anotherPlayer)
            anotherPlayer.`accepts the game invite`()
            gameCreator.`starts the game`()
        }

        When {
            gameCreator.`makes a bid`(randomBid())
        }

        Then {
            anotherPlayer.`sees bid`(UnknownBid(madeBy = gameCreator.id))
        }
    }

    @Test
    fun `bid values are revealed once everyone has made their bids`() {
        val gameCreator = scenario.newPlayer()
        val anotherPlayer = scenario.newPlayer()
        val thePlayers = listOf(gameCreator, anotherPlayer)

        Given {
            gameCreator.`creates a game`()
            gameCreator.invites(anotherPlayer)
            anotherPlayer.`accepts the game invite`()
            gameCreator.`starts the game`()
        }

        When {
            gameCreator.`makes a bid`(Bid.of(1))
            anotherPlayer.`makes a bid`(Bid.of(0))
        }

        Then {
            thePlayers.each {
                `see a bid`(Bid.of(1).madeBy(gameCreator.id))
                `see a bid`(Bid.of(0).madeBy(anotherPlayer.id))
            }
        }
    }

    private fun randomBid() = Arb.validBid.single()
}
