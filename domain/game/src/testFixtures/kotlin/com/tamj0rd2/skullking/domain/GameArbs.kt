package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.model.game.GameId
import com.tamj0rd2.skullking.domain.model.game.PlayerId
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid

object GameArbs {
    val playerIdArb = Arb.uuid(allowNilValue = false).map { PlayerId.of(it) }
    val gameIdArb = Arb.uuid(allowNilValue = false).map { GameId.of(it) }
}

fun propertyTest(block: suspend () -> Unit) = runBlocking(block)

fun <T> listOfSize(
    size: Int,
    populate: () -> T,
) = buildList { repeat(size) { add(populate()) } }
