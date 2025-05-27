package com.tamj0rd2.skullking.domain.gamev3

sealed interface GameErrorCode {
    data object NotEnoughPlayers : GameErrorCode

    data object TooManyPlayers : GameErrorCode

    data class CannotPerformActionInCurrentPhase(
        val command: GameCommand,
        val phase: GamePhase,
    ) : GameErrorCode
}
