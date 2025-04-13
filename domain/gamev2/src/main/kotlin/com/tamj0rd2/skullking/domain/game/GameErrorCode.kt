package com.tamj0rd2.skullking.domain.game

sealed class GameErrorCode : IllegalStateException() {
    override val message = this::class.simpleName

    // TODO: uncomment this to change my error codes into data objects.
//    protected fun readResolve(): Any = this

    class NotEnoughPlayersToCreateGame : GameErrorCode()

    class TooManyPlayersToCreateGame : GameErrorCode()

    class GameIdMismatch : GameErrorCode()

    class CannotPlayMoreThan10Rounds : GameErrorCode()

    class CannotStartAPreviousRound : GameErrorCode()

    class CannotStartARoundMoreThan1Ahead : GameErrorCode()

    class CannotStartARoundThatIsAlreadyInProgress : GameErrorCode()

    class CannotCompleteARoundThatIsNotInProgress : GameErrorCode()

    class CannotBidOutsideBiddingPhase : GameErrorCode()

    class AlreadyBid : GameErrorCode()
}
