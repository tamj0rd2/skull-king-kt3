package com.tamj0rd2.skullking.domain.game

sealed class GameErrorCode : IllegalStateException() {
    override val message = this::class.simpleName

    protected fun readResolve(): Any = this

    data object NotEnoughPlayersToCreateGame : GameErrorCode()

    data object TooManyPlayersToCreateGame : GameErrorCode()

    data object GameIdMismatch : GameErrorCode()

    data object CannotPlayMoreThan10Rounds : GameErrorCode()

    data object CannotStartAPreviousRound : GameErrorCode()

    data object CannotStartARoundMoreThan1Ahead : GameErrorCode()

    data object CannotStartARoundThatIsAlreadyInProgress : GameErrorCode()

    data object CannotCompleteARoundThatIsNotInProgress : GameErrorCode()

    data object CannotBidOutsideBiddingPhase : GameErrorCode()

    data object AlreadyBid : GameErrorCode()

    data class CannotStartATrickFromCurrentPhase(
        val currentPhase: GamePhase,
    ) : GameErrorCode()
}
