package com.tamj0rd2.skullking.domain.game

sealed class LobbyErrorCode : RuntimeException("")

sealed class AddPlayerErrorCode : LobbyErrorCode() {
    class GameHasAlreadyStarted : AddPlayerErrorCode()

    class LobbyIsFull : AddPlayerErrorCode()

    class PlayerHasAlreadyJoined : AddPlayerErrorCode()
}

sealed class StartGameErrorCode : LobbyErrorCode() {
    class TooFewPlayers : StartGameErrorCode()
}

class GameNotInProgress : LobbyErrorCode()
