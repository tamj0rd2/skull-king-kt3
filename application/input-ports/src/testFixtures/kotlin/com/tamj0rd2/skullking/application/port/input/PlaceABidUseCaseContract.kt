package com.tamj0rd2.skullking.application.port.input

import com.tamj0rd2.skullking.application.port.input.testsupport.Given
import com.tamj0rd2.skullking.application.port.input.testsupport.Then
import com.tamj0rd2.skullking.application.port.input.testsupport.UseCaseContract
import com.tamj0rd2.skullking.application.port.input.testsupport.When
import com.tamj0rd2.skullking.application.port.input.testsupport.each
import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.LobbyArbs.validBid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.single
import org.junit.jupiter.api.Test

interface PlaceABidUseCaseContract : UseCaseContract {
    val propertyTestIterations: Int
        get() = 1000

    @Test
    fun `when a player bids, other players can see that they have bid, but not what it is`() {
        val gameCreator = scenario.newPlayer()
        val anotherPlayer = scenario.newPlayer()

        Given {
            gameCreator.`creates a lobby`()
            gameCreator.invites(anotherPlayer)
            anotherPlayer.`accepts the lobby invite`()
            gameCreator.`starts the game`()
        }

        When { gameCreator.`places a bid`(randomBid()) }

        Then { anotherPlayer.`sees that a bid has been placed by`(gameCreator.id) }
    }

    @Test
    fun `bid values are revealed once everyone has placed their bids`() {
        val gameCreator = scenario.newPlayer()
        val anotherPlayer = scenario.newPlayer()
        val thePlayers = listOf(gameCreator, anotherPlayer)

        Given {
            gameCreator.`creates a lobby`()
            gameCreator.invites(anotherPlayer)
            anotherPlayer.`accepts the lobby invite`()
            gameCreator.`starts the game`()
        }

        When {
            gameCreator.`places a bid`(Bid.of(1))
            anotherPlayer.`places a bid`(Bid.of(0))
        }

        Then {
            thePlayers.each {
                `see a bid`(Bid.of(1), placedBy = gameCreator.id)
                `see a bid`(Bid.of(0), placedBy = anotherPlayer.id)
            }
        }
    }

    private fun randomBid() = Arb.validBid.single()
}
