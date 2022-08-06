package dev.foraged.foxtrot.map

import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.DoublePersistMap
import org.bukkit.Bukkit

@RegisterMap
object BountyPersistMap : DoublePersistMap("Bounty", "Bounty", true, Bukkit.getServerName())