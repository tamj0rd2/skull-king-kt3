package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.domain.game.GameId

fun interface ViewGamesUseCase : UseCase<ViewGamesInput, ViewGamesOutput>

data object ViewGamesInput

data class ViewGamesOutput(val games: List<GamesListItem>)

data class GamesListItem(val id: GameId)
