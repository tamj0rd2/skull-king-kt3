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
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import kotlin.test.Test
import kotlin.test.assertEquals

class GameExampleTest {
    @Test
    fun `example - phase transitions - playing a 2 player game up until the end of round 1`() {
        val player1 = PlayerId.random()
        val player2 = PlayerId.random()
        val game = Game.new(setOf(player1, player2)).orThrow()

        game.execute(StartRound(roundNumber = RoundNumber.One)).orThrow()
        assertEquals(GamePhase.Bidding, game.state.phase, "starting a round transitions to correct phase")

        game.execute(PlaceABid(actor = player1, bid = Bid.One)).orThrow()
        game.execute(PlaceABid(actor = player2, bid = Bid.Zero)).orThrow()
        game.execute(StartTrick(trickNumber = TrickNumber.of(1))).orThrow()
        assertEquals(GamePhase.TrickTaking, game.state.phase, "starting a trick transitions to correct phase")

        game.execute(PlayACard(actor = player1, card = CannedCard)).orThrow()
        game.execute(PlayACard(actor = player2, card = CannedCard)).orThrow()
        game.execute(CompleteTrick(trickNumber = TrickNumber.of(1))).orThrow()
        assertEquals(GamePhase.TrickScoring, game.state.phase, "completing a trick transitions to correct phase")

        game.execute(CompleteRound(roundNumber = RoundNumber.One)).orThrow()
        assertEquals(GamePhase.AwaitingNextRound, game.state.phase, "completing a round transitions to correct phase")
    }
}
