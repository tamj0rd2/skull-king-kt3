package com.tamj0rd2.skullking.domain.gamev3

sealed interface GameEvent

data object GameStartedEvent : GameEvent
