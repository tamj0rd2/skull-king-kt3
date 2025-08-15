package com.tamj0rd2.skullking.application.ports.input

fun interface ViewGamesUseCase : UseCase<ViewGamesInput, ViewGamesOutput>

data object ViewGamesInput

data class ViewGamesOutput(val games: List<GameListItem>)

data object GameListItem
