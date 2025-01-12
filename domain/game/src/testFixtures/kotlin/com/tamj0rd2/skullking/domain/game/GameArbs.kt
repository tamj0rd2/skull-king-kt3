package com.tamj0rd2.skullking.domain.game

import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid

object GameArbs {
    val playerIdArb = Arb.uuid(allowNilValue = false).map { PlayerId.of(it) }
    val gameIdArb = Arb.uuid(allowNilValue = false).map { GameId.of(it) }
    val Arb.Companion.validPlayerCountToStartAGame get() = Arb.int(min = Game.MINIMUM_PLAYER_COUNT, max = Game.MAXIMUM_PLAYER_COUNT)
    val Arb.Companion.validBid get() = Arb.int(min = 0, max = 10).map { Bid.of(it) }
}

fun propertyTest(block: suspend () -> Unit) = runBlocking(block)

fun <T> listOfSize(
    size: Int,
    populate: () -> T,
): List<T> {
    require(size >= 0) { "size cannot be negative" }
    return buildList { repeat(size) { add(populate()) } }
}
