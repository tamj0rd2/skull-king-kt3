package com.tamj0rd2.skullking.domain.gamev2

import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.gamev2.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.gamev2.values.Bid
import com.tamj0rd2.skullking.domain.gamev2.values.RoundNumber
import com.tamj0rd2.skullking.domain.gamev2.values.TrickNumber
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.values.random
import kotlin.test.Test
import kotlin.test.assertEquals

class GameExampleTest {
    @Test
    fun `example - phase transitions - playing a 2 player game up until the end of round 1`() {
        val player1 = PlayerId.random()
        val player2 = PlayerId.random()

        Game
            .new(setOf(player1, player2))
            .executeAndAssert(
                act = { it.execute(StartRound(roundNumber = RoundNumber.One)) },
                assert = { assertEquals(GamePhase.Bidding, it.state.phase, "starting a round transitions to correct phase") },
            ).executeAndAssert(
                act = { it ->
                    it
                        .execute(PlaceABid(actor = player1, bid = Bid.One))
                        .flatMap { it.execute(PlaceABid(actor = player2, bid = Bid.Zero)) }
                        .flatMap { it.execute(StartTrick(trickNumber = TrickNumber.One)) }
                },
                assert = { assertEquals(GamePhase.TrickTaking, it.state.phase, "starting a trick transitions to correct phase") },
            ).executeAndAssert(
                act = { it ->
                    it
                        .execute(PlayACard(actor = player1, card = CannedCard))
                        .flatMap { it.execute(PlayACard(actor = player2, card = CannedCard)) }
                        .flatMap { it.execute(CompleteTrick(trickNumber = TrickNumber.One)) }
                },
                assert = { assertEquals(GamePhase.TrickScoring, it.state.phase, "completing a trick transitions to correct phase") },
            ).executeAndAssert(
                act = { it.execute(CompleteRound(roundNumber = RoundNumber.One)) },
                assert = { assertEquals(GamePhase.AwaitingNextRound, it.state.phase, "completing a round transitions to correct phase") },
            )
    }
}
