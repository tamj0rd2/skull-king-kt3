package com.tamj0rd2.skullking.domain.game

sealed class GameErrorCode : IllegalStateException("") {
    class NotEnoughPlayersToCreateGame : GameErrorCode()

    class TooManyPlayersToCreateGame : GameErrorCode()

    class GameIdMismatch : GameErrorCode()

    class CannotPlayMoreThan10Rounds : GameErrorCode()

    class CannotStartAPreviousRound : GameErrorCode()

    class CannotStartARoundMoreThan1Ahead : GameErrorCode()

    class CannotStartARoundThatIsAlreadyInProgress : GameErrorCode()

    class CannotCompleteARoundThatIsNotInProgress : GameErrorCode()
}
