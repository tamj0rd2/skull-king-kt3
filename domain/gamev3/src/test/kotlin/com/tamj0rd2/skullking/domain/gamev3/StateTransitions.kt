package com.tamj0rd2.skullking.domain.gamev3

import com.tamj0rd2.skullking.domain.gamev3.GameArbs.andACommand
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.command
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.game
import com.tamj0rd2.skullking.domain.gamev3.GameArbs.validOnly
import com.tamj0rd2.skullking.domain.gamev3.GameState.AwaitingNextRound
import com.tamj0rd2.skullking.domain.gamev3.GameState.Bidding
import com.tamj0rd2.skullking.domain.gamev3.GameStats.collectCommand
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.assumeWasSuccessful
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propTestConfig
import com.tamj0rd2.skullking.domain.gamev3.PropertyTesting.propertyTest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isContainedIn

class StateTransitions {
    @Test
    fun `a game in the AwaitingNextRound state can only ever transition to Bidding`() {
        propertyTest {
            checkAll(
                propTestConfig,
                Arb.game.validOnly().filter { it.state is AwaitingNextRound },
                Arb.command,
            ) { initialGame, command ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                expectThat(updatedGame.state).isA<Bidding>()
            }
        }
    }

    @Test
    fun `a game in the Bidding state can only ever transition to TrickTaking`() {
        propertyTest {
            checkAll(
                propTestConfig,
                // TODO: can we filter while the arbs are being generated rather than after? might need to write my own arb
                Arb.game
                    .validOnly()
                    .filter { it.state is Bidding }
                    .andACommand,
            ) { (initialGame, command) ->
                val updatedGame = initialGame.execute(command).assumeWasSuccessful()
                collectCommand(command)
                expectThat(updatedGame.state.name).isContainedIn(setOf(GameStateName.Bidding, GameStateName.TrickTaking))
            }
        }
    }
}
