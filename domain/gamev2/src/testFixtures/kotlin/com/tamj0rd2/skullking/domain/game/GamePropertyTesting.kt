package com.tamj0rd2.skullking.domain.game

import dev.forkhandles.result4k.orThrow
import dev.forkhandles.values.random

@Deprecated("delete this")
val somePlayers = setOf(PlayerId.random(), PlayerId.random())

fun Game.mustExecute(command: GameCommand) = execute(command).orThrow()
