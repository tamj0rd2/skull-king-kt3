package com.tamj0rd2.skullking.domain

import com.tamj0rd2.skullking.domain.GameActionArbs.gameActionsArb
import com.tamj0rd2.skullking.domain.model.PlayerId
import com.tamj0rd2.skullking.domain.model.game.Game
import com.tamj0rd2.skullking.domain.model.game.GameId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
import io.kotest.common.runBlocking
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterIsInstance
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.uuid

object GameArbs {
    val playerIdArb = Arb.uuid(allowNilValue = false).map { PlayerId.of(it) }
    val gameIdArb = Arb.uuid(allowNilValue = false).map { GameId.of(it) }

    val gameArb =
        gameActionsArb
            .map { actions ->
                val game = Game.new()
                game.execute(*actions.toTypedArray()).map { game }
            }.ignoreFailures()
}

fun <T, E : Throwable> Arb<Result4k<T, E>>.ignoreFailures() = filterIsInstance<Success<T>>().map { it.value }

fun propertyTest(block: suspend () -> Unit) = runBlocking(block)

fun <T> listOfSize(
    size: Int,
    populate: () -> T,
) = buildList { repeat(size) { add(populate()) } }
