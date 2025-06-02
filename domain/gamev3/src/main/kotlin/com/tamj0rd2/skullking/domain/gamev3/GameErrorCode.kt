package com.tamj0rd2.skullking.domain.gamev3

sealed class GameErrorCode : IllegalStateException() {
    protected fun readResolve(): Any = this

    data object NotEnoughPlayers : GameErrorCode()

    data object TooManyPlayers : GameErrorCode()

    data class CannotApplyEventInCurrentState(
        val event: GameEvent,
        val phase: GameState,
    ) : GameErrorCode()

    sealed class CannotReconstituteGame : GameErrorCode() {
        data object NoEvents : CannotReconstituteGame()

        data object InvalidFirstEvent : CannotReconstituteGame()

        data object MultipleGameIds : CannotReconstituteGame()
    }

    data object NotYetImplemented : GameErrorCode()
}
