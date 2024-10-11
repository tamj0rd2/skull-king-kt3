package com.tamj0rd2.skullking.domain.model.game

import com.tamj0rd2.skullking.domain.GameArbs.gameArb
import com.tamj0rd2.skullking.domain.GameArbs.playerIdArb
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MAXIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.model.game.Game.Companion.MINIMUM_PLAYER_COUNT
import com.tamj0rd2.skullking.domain.propertyTest
import com.tamj0rd2.skullking.domain.wasSuccessful
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.next
import io.kotest.property.assume
import io.kotest.property.checkAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.all
import strikt.assertions.containsExactly
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.one

class GameTests {
    @Test
    fun `games always start with a GameCreated event`() =
        propertyTest {
            checkAll(gameArb) { game ->
                expectThat(game.events).first().isA<GameCreated>()
            }
        }

    @Test
    fun `games always have a single GameCreated event`() =
        propertyTest {
            checkAll(gameArb) { game ->
                expectThat(game.events).one { isA<GameCreated>() }
            }
        }

    @Test
    fun `a game can be restored using its history of events`() =
        propertyTest {
            checkAll(gameArb) { game ->
                val restoredGame = Game.from(game.events)
                expectThat(restoredGame) {
                    get { events }.isEqualTo(game.events)
                    get { state }.isEqualTo(game.state)
                }
            }
        }

    @Test
    fun `all events within a game relate to that specific game`() =
        propertyTest {
            checkAll(gameArb) { game ->
                expectThat(game.events).all { get { gameId }.isEqualTo(game.id) }
            }
        }

    // TODO: replace this with: a game never has more than 6 players
    @Test
    fun `joining a full game is not possible`() =
        propertyTest {
            checkAll(
                gameArb.filter { it.state.players.size == MAXIMUM_PLAYER_COUNT },
                playerIdArb,
            ) { game, playerWhoWantsToJoin ->
                val initialPlayers = game.state.players
                val initialEvents = game.events

                assume(initialPlayers.size == MAXIMUM_PLAYER_COUNT)
                assume(!initialPlayers.contains(playerWhoWantsToJoin))

                expectThat(game.addPlayer(playerWhoWantsToJoin)).isA<Failure<GameIsFull>>()
                expectThat(game).run {
                    get { state.players }.isEqualTo(initialPlayers)
                    get { events }.isEqualTo(initialEvents)
                }
            }
        }

    // TODO: replace this with: the players in a game are always unique
    @Test
    fun `a player cannot join the same game twice`() =
        propertyTest {
            checkAll(
                gameArb.filter { it.state.players.size < MAXIMUM_PLAYER_COUNT - 2 },
                playerIdArb,
            ) { game, playerWhoWantsToJoin ->
                assume(game.state.players.size <= MAXIMUM_PLAYER_COUNT - 2)
                assume(!game.state.players.contains(playerWhoWantsToJoin))

                expectThat(game.addPlayer(playerWhoWantsToJoin))
                    .describedAs("joining the first time")
                    .wasSuccessful()
                val playersBeforeSecondJoin = game.state.players
                val eventsBeforeSecondJoin = game.events

                expectThat(game.addPlayer(playerWhoWantsToJoin))
                    .describedAs("trying to join again")
                    .isA<Failure<PlayerHasAlreadyJoined>>()
                expectThat(game) {
                    get { state.players }.isEqualTo(playersBeforeSecondJoin)
                    get { events }.isEqualTo(eventsBeforeSecondJoin)
                }
            }
        }

    // TODO: this seems like it should be the invariant of a Hand model.
    @Test
    @Disabled
    fun `within the players hands, there can't be more cards than exist of that type (new cards aren't invented from thin air)`() {
        TODO()
    }

    @Nested
    inner class StartGameTests {
        // TODO: delete once covered in use case
        @Test
        fun `when the game is started, each player is dealt 1 card`() {
            val game = gameArb.filter { it.state.players.size >= MINIMUM_PLAYER_COUNT }.next()
            val players = game.state.players

            game.start().orThrow()

            expectThat(game.state.playerHands.keys).containsExactly(players)
            expectThat(game.state.playerHands.values).all { hasSize(1) }
        }

        // TODO: delete once covered in use case
        @Test
        fun `the game cannot be started with less than 2 players`() {
            // TODO: make this a property test

            val game = Game.new()
            game.addPlayer(PlayerId.random()).orThrow()
            expectThrows<StartGameErrorCode> { game.start().orThrow() }
        }
    }
}
