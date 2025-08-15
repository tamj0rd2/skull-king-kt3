package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface ViewGamesUseCase : UseCase<ViewGamesInput, ViewGamesOutput>

data object ViewGamesInput

data class ViewGamesOutput(val games: List<GameListItem>)

data class GameListItem(val id: GameId, val host: PlayerId)
