package com.tamj0rd2.skullking.application.ports.input

import com.tamj0rd2.skullking.domain.game.Bid
import com.tamj0rd2.skullking.domain.game.GameId
import com.tamj0rd2.skullking.domain.game.PlayerId

fun interface PlaceBidUseCase : UseCase<PlaceBidInput, PlaceBidOutput>

data class PlaceBidInput(val gameId: GameId, val playerId: PlayerId, val bid: Bid)

data object PlaceBidOutput
