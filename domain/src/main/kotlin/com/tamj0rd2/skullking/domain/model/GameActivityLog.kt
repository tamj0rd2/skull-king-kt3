package com.tamj0rd2.skullking.domain.model

internal class GameActivityLog private constructor() {
    private constructor(history: List<GameEvent>) : this() {
        check(history.isNotEmpty()) { "Provided history was empty. Create a new game instead." }

        val gameId = history.first().gameId
        check(history.all { it.gameId == gameId }) { "GameId mismatch" }

        this.history = history
    }

    lateinit var history: List<GameEvent>
        private set

    private val _updates = mutableListOf<GameEvent>()
    val updates: List<GameEvent> get() = _updates

    val gameId get() = (history + updates).first().gameId

    private var shouldRecord = false

    fun startRecordingUpdates() {
        shouldRecord = true
    }

    fun record(event: GameEvent) {
        if (shouldRecord) _updates.add(event)
    }

    companion object {
        fun forNewGame() = GameActivityLog().apply { startRecordingUpdates() }

        fun forExistingGame(history: List<GameEvent>) = GameActivityLog(history)
    }
}
