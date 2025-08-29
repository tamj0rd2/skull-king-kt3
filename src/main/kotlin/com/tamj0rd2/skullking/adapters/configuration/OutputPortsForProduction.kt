package com.tamj0rd2.skullking.adapters.configuration

import com.tamj0rd2.skullking.adapters.inmemory.inMemory
import com.tamj0rd2.skullking.application.ports.output.OutputPorts

fun OutputPorts.Companion.forProduction(): OutputPorts = inMemory()
