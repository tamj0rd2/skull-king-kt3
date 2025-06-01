package com.tamj0rd2.skullking.domain.gamev3

sealed interface GameErrorCode {
    data object NotEnoughPlayers : GameErrorCode

    data object TooManyPlayers : GameErrorCode

    data class CannotApplyEventInCurrentState(
        val event: GameEvent,
        val phase: GameState,
    ) : GameErrorCode

    sealed interface CannotReconstituteGame : GameErrorCode {
        data object NoEvents : CannotReconstituteGame

        data object InvalidFirstEvent : CannotReconstituteGame

        data object MultipleGameIds : CannotReconstituteGame
    }

    data class NotYetImplemented(
        val message: String,
    ) : GameErrorCode
}
