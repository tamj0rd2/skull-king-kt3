package com.tamj0rd2.skullking.domain.game

import com.tamj0rd2.skullking.domain.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.game.GameErrorCode.NotEnoughPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.GameErrorCode.TooManyPlayersToCreateGame
import com.tamj0rd2.skullking.domain.game.PropertyTesting.propertyTest
import dev.forkhandles.result4k.Failure
import dev.forkhandles.values.random
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.checkAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA

@Nested
class StartingAGame {
    @Test
    fun `cannot create a game with less than 2 players`() =
        propertyTest {
            checkAll(Arb.nonNegativeInt(max = MINIMUM_PLAYER_COUNT - 1)) { playerCount ->
                val playerIds = buildSet { repeat(playerCount) { add(PlayerId.random()) } }
                expectThat(Game.new(playerIds)).isA<Failure<NotEnoughPlayersToCreateGame>>()
            }
        }

    @Test
    fun `cannot create a game with more than 6 players`() =
        propertyTest {
            checkAll(Arb.int(min = MAXIMUM_PLAYER_COUNT + 1, max = 100)) { playerCount ->
                val playerIds = buildSet { repeat(playerCount) { add(PlayerId.random()) } }
                expectThat(Game.new(playerIds)).isA<Failure<TooManyPlayersToCreateGame>>()
            }
        }
}
