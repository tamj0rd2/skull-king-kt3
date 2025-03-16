package com.tamj0rd2.skullking.domain.game

sealed class GameErrorCode : IllegalStateException() {
    class NotEnoughPlayersToStartGame : GameErrorCode()

    class NotImplemented : GameErrorCode()
}
