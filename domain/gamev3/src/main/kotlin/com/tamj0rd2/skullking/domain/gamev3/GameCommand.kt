package com.tamj0rd2.skullking.domain.gamev3

sealed interface GameCommand

data object StartRoundCommand : GameCommand

data class PlaceBidCommand(
    val playerId: PlayerId,
    val bid: SomeBid,
) : GameCommand
