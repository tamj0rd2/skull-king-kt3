package com.tamj0rd2.skullking.domain

interface Event<out ID> {
    val entityId: ID
}
