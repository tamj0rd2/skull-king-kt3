package com.tamj0rd2.skullking.domain.gamev3

sealed interface GameCommand

data object StartRoundCommand : GameCommand

sealed interface PlayerOriginatedCommand {
    val playerId: PlayerId
}

data class PlaceBidCommand(override val playerId: PlayerId, val bid: SomeBid) :
    GameCommand, PlayerOriginatedCommand

data object StartTrickCommand : GameCommand

data class PlayCardCommand(override val playerId: PlayerId, val card: Card) :
    GameCommand, PlayerOriginatedCommand
