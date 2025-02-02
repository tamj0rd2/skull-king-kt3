package com.tamj0rd2.skullking.domain.game

data class Notifications internal constructor(
    private val notifications: Map<Version, List<LobbyNotification>>,
) {
    val all = notifications.toSortedMap().flatMap { it.value }

    internal fun add(
        version: Version,
        newNotifications: List<LobbyNotification>,
    ): Notifications {
        val updatedNotificationsForVersion = notifications.getOrDefault(version, emptyList()) + newNotifications
        return copy(notifications = notifications + Pair(version, updatedNotificationsForVersion))
    }

    @Deprecated("BAD")
    fun sinceVersion(version: Version) =
        notifications
            .filterKeys { it > version }
            .toSortedMap()
            .flatMap { it.value }

    fun forVersion(version: Version): List<LobbyNotification> = notifications.getValue(version)
}
