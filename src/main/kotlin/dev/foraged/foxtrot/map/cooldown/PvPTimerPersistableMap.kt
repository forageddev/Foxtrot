package dev.foraged.foxtrot.map.cooldown

import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.CooldownPersistMap

@RegisterMap
object PvPTimerPersistableMap : CooldownPersistMap("PvPTimers", "PvPTimer", false)