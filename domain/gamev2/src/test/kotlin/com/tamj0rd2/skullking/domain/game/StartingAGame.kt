package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.GameEvent.GameStarted
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.first
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@Nested
class StartingAGame {
    @Test
    fun `a new game starts with a GameStarted event`() {
        val game = Game(somePlayers)
        expectThat(game.events)
            .first()
            .isA<GameStarted>()
            .get { players }
            .isEqualTo(somePlayers)
    }

    @Test
    @Disabled
    fun `cannot start a game without players`() {
        TODO("not yet implemented")
    }

    @Test
    @Disabled
    fun `cannot start a game with a single player`() {
        TODO("not yet implemented")
    }
}
