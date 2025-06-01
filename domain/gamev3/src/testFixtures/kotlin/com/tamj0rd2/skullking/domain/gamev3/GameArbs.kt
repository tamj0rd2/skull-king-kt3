package com.tamj0rd2.skullking.domain.gamev3

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.valueOrNull
import dev.forkhandles.values.random
import io.kotest.common.DelicateKotest
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.set
import io.kotest.property.resolution.default
import kotlin.reflect.KClass

object GameArbs {
    val Arb.Companion.command get() = Arb.default<GameCommand>()
    val Arb.Companion.game get() = Arb.choice(Arb.gameBuiltFromScratch, Arb.reconstitutedGame)

    private val Arb.Companion.gameBuiltFromScratch
        get() =
            Arb.bind(
                // TODO: use constants from the domain.
                Arb.set(Arb.playerId, 2..6),
                Arb.list(Arb.command),
            ) { playerIds, commands ->
                commands.fold(Game.new(playerIds)) { result, command ->
                    result.flatMap { it.execute(command) }
                }
            }

    private val Arb.Companion.reconstitutedGame get() =
        Arb.choice(
            // TODO: relax the range here.
            Arb.list(Arb.event, 0..20).map { Game.reconstitute(it) },
            // NOTE: add cases where the events all relate to the same game. That should make the gen faster.
            // NOTE: also add cases where the gameStarted event is first. That should make the gen faster.
        )

    private val Arb.Companion.gameId get() = arbitrary { SomeGameId.random(it.random) }
    private val Arb.Companion.playerId get() = arbitrary { SomePlayerId.random(it.random) }

    private val providedArbs =
        mapOf<KClass<*>, Arb<*>>(
            GameId::class to Arb.gameId,
            PlayerId::class to Arb.playerId,
            GameStartedEvent::class to Arb.gameStartedEvent,
        )

    @OptIn(DelicateKotest::class)
    @Suppress("NO_REFLECTION_IN_CLASS_PATH")
    private val Arb.Companion.event get() = Arb.sealed<GameEvent>()

    private val Arb.Companion.gameStartedEvent
        get() =
            Arb
                .bind(
                    Arb.gameId,
                    Arb.set(Arb.playerId, 0..10),
                    GameStartedEvent::new,
                ).validOnly()

    fun <T> Arb<Result4k<T, *>>.validOnly(): Arb<T> = filter { it is Success }.map { it.valueOrNull()!! }

    @OptIn(DelicateKotest::class)
    @Suppress("NO_REFLECTION_IN_CLASS_PATH", "UNCHECKED_CAST")
    private inline fun <reified T> Arb.Companion.sealed(): Arb<T> {
        val clazz = T::class
        if (!clazz.isSealed) error("Class ${clazz.simpleName} is not sealed")

        return Arb.choice(
            clazz.sealedSubclasses.map { subclass ->
                subclass.objectInstance?.let { Arb.constant(it) }
                    ?: providedArbs[subclass]
                    ?: Arb.bind(providedArbs, subclass)
            },
        ) as Arb<T>
    }
}
