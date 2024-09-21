package com.tamj0rd2.skullking.domain

import net.jqwik.api.domains.Domain

@Domain(StdLibArbs::class)
@Domain(GameArbs::class)
@Domain(GameEventArbs::class)
@Domain(PlayerArbs::class)
annotation class SkullKingArbs
