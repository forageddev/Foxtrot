package dev.foraged.foxtrot.hologram.impl

import dev.foraged.foxtrot.hologram.FoxtrotHologram
import dev.foraged.foxtrot.map.stats.KillsPersistMap
import dev.foraged.foxtrot.map.stats.KillstreakPersistMap
import net.evilblock.cubed.util.CC
import org.bukkit.Location

class KillstreakHologram(location: Location) : FoxtrotHologram(KillstreakPersistMap, "${CC.B_PRI}Top Killstreak Leaderboard", location)