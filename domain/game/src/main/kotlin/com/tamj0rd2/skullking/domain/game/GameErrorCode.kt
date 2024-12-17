package com.tamj0rd2.skullking.domain.game

sealed class GameErrorCode : RuntimeException("")

sealed class AddPlayerErrorCode : GameErrorCode() {
    class GameHasAlreadyStarted : AddPlayerErrorCode()

    class GameIsFull : AddPlayerErrorCode()

    class PlayerHasAlreadyJoined : AddPlayerErrorCode()
}

sealed class StartGameErrorCode : GameErrorCode() {
    class TooFewPlayers : StartGameErrorCode()
}
