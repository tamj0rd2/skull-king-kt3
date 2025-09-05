package com.tamj0rd2.skullking.domain.game

import com.natpryce.hamkrest.allElements
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.has
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isA
import net.jqwik.api.Arbitraries
import net.jqwik.api.Arbitrary
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.Provide
import net.jqwik.kotlin.api.anyForSubtypeOf
import net.jqwik.kotlin.api.anyForType
import net.jqwik.kotlin.api.combine

class GameInvariantsTest {
    @Property
    fun `a game always start with a GameCreated event`(@ForAll("gameArb") game: Game) {
        assertThat(game.events.first(), isA<GameEvent.GameCreated>())
    }

    @Property
    fun `a game only ever has 1 GameCreated event`(@ForAll("gameArb") game: Game) {
        assertThat(game.events.filterIsInstance<GameEvent.GameCreated>(), hasSize(equalTo(1)))
    }

    @Property
    fun `each event in a game is related to that specific game`(@ForAll("gameArb") game: Game) {
        assertThat(game.events, allElements(has(GameEvent::gameId, equalTo(game.id))))
    }

    @Property
    fun `each successful command results in 1 new event being emitted`(
        @ForAll("gameArb") initialGame: Game,
        @ForAll("commandArb") command: GameCommand,
    ) {
        val updatedGame = initialGame.execute(command)
        assertThat(updatedGame.events, hasSize(equalTo(initialGame.events.size + 1)))
    }

    @Property
    fun `each successful command results in a state change`(
        @ForAll("gameArb") initialGame: Game,
        @ForAll("commandArb") command: GameCommand,
    ) {
        val updatedGame = initialGame.execute(command)
        assertThat(updatedGame, !equalTo(initialGame))
    }

    @Property
    fun `successful commands never change the game's ID`(@ForAll("gameArb") initialGame: Game, @ForAll("commandArb") command: GameCommand) {
        val updatedGame = initialGame.execute(command)
        assertThat(updatedGame.id, equalTo(initialGame.id))
    }

    @Property
    fun `a game reconstituted from events is equal to the game those events came from`(@ForAll("gameArb") originalGame: Game) {
        val reconstituted = Game.reconstitute(originalGame.events)
        assertThat(reconstituted, equalTo(originalGame))
    }

    @Provide private fun playerIdArb() = anyForType<PlayerId>()

    @Provide
    private fun commandArb(): Arbitrary<GameCommand> =
        anyForSubtypeOf<GameCommand> {
            provide<GameCommand.StartGame> { Arbitraries.create { GameCommand.StartGame } }
            provide<GameCommand.AddPlayer> {
                combine {
                    val playerId by playerIdArb()

                    combineAs { GameCommand.AddPlayer(playerId) }
                }
            }
        }

    @Provide
    private fun gameArb(): Arbitrary<Game> = combine {
        val creator by playerIdArb()
        val commands by commandArb().list()

        combineAs { commands.fold(Game.new(creator)) { game, command -> game.execute(command) } }
    }
}
