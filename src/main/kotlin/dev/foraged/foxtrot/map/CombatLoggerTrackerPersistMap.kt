package dev.foraged.foxtrot.map

import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.BooleanPersistMap

@RegisterMap
object CombatLoggerTrackerPersistMap : BooleanPersistMap("LoggerDied", "LoggerDied", true)