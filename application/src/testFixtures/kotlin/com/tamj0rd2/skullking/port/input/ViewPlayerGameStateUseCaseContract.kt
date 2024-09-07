package com.tamj0rd2.skullking.port.input

import com.tamj0rd2.skullking.domain.model.GameId
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.PlayerJoined
import com.tamj0rd2.skullking.port.input.ViewPlayerGameStateUseCase.ViewPlayerGameStateQuery
import com.tamj0rd2.skullking.port.output.GameEventsPort
import dev.forkhandles.values.random
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

abstract class ViewPlayerGameStateUseCaseContract {
    // TODO: I don't really like that I'm exposing the GameEventsPort here. It means that the http/ws versions of the game
    //  will also need an adapter in their tests... lets see.
    protected abstract val gameEventsPort: GameEventsPort
    protected abstract val useCase: ViewPlayerGameStateUseCase


    // TODO: In this test alone, it's already slightly hard to follow who the actor etc is because there are a few players in the game.
    //  roles could help... And maybe also address the first issue above.
    @Test
    fun `can view the game state`() {
        val gameId = GameId.random()
        val thisPlayer = PlayerId.random()
        val playersInTheGame = listOf(PlayerId.random(), thisPlayer, PlayerId.random())

        gameEventsPort.save(playersInTheGame.map { PlayerJoined(gameId, it) })

        val gameState = useCase(
            ViewPlayerGameStateQuery(
                gameId = gameId,
                playerId = thisPlayer
            )
        )

        expectThat(gameState) {
            get { players }.describedAs("players").isEqualTo(playersInTheGame)
        }
    }
}
