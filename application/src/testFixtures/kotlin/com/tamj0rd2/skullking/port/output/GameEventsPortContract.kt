package com.tamj0rd2.skullking.port.output

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

abstract class GameEventsPortContract {
    protected abstract val gameEventsPort: GameRepository

    @Test
    fun `can save and retrieve game events`() {
        val gameId = GameId.random()
        val event = PlayerJoined(gameId, PlayerId.random())

        gameEventsPort.saveGameEvents(listOf(event))
        expectThat(gameEventsPort.findGameEvents(gameId)).isEqualTo(listOf(event))
    }

    @Test
    fun `searching for a specific game will not bring back game events for a different game`() {
        val gameIWant = GameId.random()
        val someOtherGame = GameId.random()

        gameEventsPort.saveGameEvents(
            listOf(
                PlayerJoined(gameIWant, PlayerId.random()),
                PlayerJoined(someOtherGame, PlayerId.random()),
                PlayerJoined(gameIWant, PlayerId.random()),
            ),
        )
        expectThat(gameEventsPort.findGameEvents(gameIWant)).all { get { gameId }.isEqualTo(gameIWant) }.hasSize(2)
    }
}
