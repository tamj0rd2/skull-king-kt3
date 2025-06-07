package com.tamj0rd2.skullking.domain.game

import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid

object LobbyArbs {
    val playerIdArb = Arb.uuid(allowNilValue = false).map { PlayerId.of(it) }
    val lobbyIdArb = Arb.uuid(allowNilValue = false).map { LobbyId.of(it) }
    val Arb.Companion.validPlayerCountToStartAGame
        get() = Arb.int(min = Lobby.MINIMUM_PLAYER_COUNT, max = Lobby.MAXIMUM_PLAYER_COUNT)

    val Arb.Companion.validBid
        get() = Arb.int(min = 0, max = 10).map { Bid.of(it) }
}

fun propertyTest(block: suspend () -> Unit) = runBlocking(block)

fun <T> listOfSize(size: Int, populate: () -> T): List<T> {
    require(size >= 0) { "size cannot be negative" }
    return buildList { repeat(size) { add(populate()) } }
}
