package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteRound
import com.tamj0rd2.skullking.domain.game.GameCommand.CompleteTrick
import com.tamj0rd2.skullking.domain.game.GameCommand.PlaceABid
import com.tamj0rd2.skullking.domain.game.GameCommand.PlayACard
import com.tamj0rd2.skullking.domain.game.GameCommand.StartRound
import com.tamj0rd2.skullking.domain.game.GameCommand.StartTrick
import com.tamj0rd2.skullking.domain.game.values.Bid
import com.tamj0rd2.skullking.domain.game.values.RoundNumber
import com.tamj0rd2.skullking.domain.game.values.TrickNumber
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

        game.execute(StartRound(roundNumber = RoundNumber.of(1))).orThrow()
        assertEquals(GamePhase.Bidding, game.state.phase)

        game.execute(PlaceABid(actor = player1, bid = Bid.one)).orThrow()
        game.execute(PlaceABid(actor = player2, bid = Bid.zero)).orThrow()
        game.execute(StartTrick(trickNumber = TrickNumber.of(1))).orThrow()
        assertEquals(GamePhase.TrickTaking, game.state.phase)

        game.execute(PlayACard(actor = player1, card = CannedCard)).orThrow()
        game.execute(PlayACard(actor = player2, card = CannedCard)).orThrow()
        game.execute(CompleteTrick(trickNumber = TrickNumber.of(1))).orThrow()
        assertEquals(GamePhase.TrickScoring, game.state.phase)

        game.execute(CompleteRound(roundNumber = RoundNumber.of(1))).orThrow()
        // TODO: introduce this assertion
//        assertEquals(GamePhase.AwaitingNextRound, game.state.phase)
    }
}
