package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.result4k.peek
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.getValue
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class Examples {
    @Test
    fun `playing a 2 player game from start to finish`() {
        val player1 = SomePlayerId.random()
        val player2 = SomePlayerId.random()

        Game
            .new(setOf(player1, player2))
            .flatMap { it.execute(StartRoundCommand) }
            .flatMap { it.execute(PlaceBidCommand(player1, SomeBid.One)) }
            .flatMap { it.execute(PlaceBidCommand(player2, SomeBid.Zero)) }
            .peek {
                expectThat(it.state).isA<GameState.Bidding>().and {
                    get { bids }.getValue(player1).isEqualTo(SomeBid.One)
                    get { bids }.getValue(player2).isEqualTo(SomeBid.Zero)
                }
            }.flatMap { it.execute(StartTrickCommand) }
            .peek {
                expectThat(it.state).isA<GameState.TrickTaking>().and {
                    get { roundNumber }.isEqualTo(RoundNumber.One)
                    get { trickNumber }.isEqualTo(TrickNumber.One)
                }
            }.orThrow()
    }
}
