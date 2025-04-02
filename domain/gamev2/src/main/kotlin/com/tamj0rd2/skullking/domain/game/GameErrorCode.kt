package com.tamj0rd2.skullking.domain.game

sealed class GameErrorCode : IllegalStateException("") {
    class NotEnoughPlayersToCreateGame : GameErrorCode()

    class TooManyPlayersToCreateGame : GameErrorCode()

    class AlreadyStartedMoreThan10Rounds : GameErrorCode()

    class AlreadyCompletedMoreThan10Rounds : GameErrorCode()

    class GameIdMismatch : GameErrorCode()
}
