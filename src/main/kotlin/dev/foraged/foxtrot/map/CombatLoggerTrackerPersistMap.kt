package dev.foraged.foxtrot.map

import dev.foraged.commons.persist.impl.BooleanPersistMap

object CombatLoggerTrackerPersistMap : BooleanPersistMap("LoggerDied", "LoggerDied", true)