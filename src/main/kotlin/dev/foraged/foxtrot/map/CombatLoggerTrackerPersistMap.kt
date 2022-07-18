package dev.foraged.foxtrot.map

import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.BooleanPersistMap
import org.bukkit.Bukkit

@RegisterMap
object CombatLoggerTrackerPersistMap : BooleanPersistMap("LoggerDied", "LoggerDied", true, Bukkit.getServerName())