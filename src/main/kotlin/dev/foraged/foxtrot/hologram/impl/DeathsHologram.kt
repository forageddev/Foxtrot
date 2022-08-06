package dev.foraged.foxtrot.hologram.impl

import dev.foraged.foxtrot.hologram.FoxtrotHologram
import dev.foraged.foxtrot.map.stats.DeathsPersistMap
import dev.foraged.foxtrot.map.stats.KillsPersistMap
import net.evilblock.cubed.util.CC
import org.bukkit.Location

class DeathsHologram(location: Location) : FoxtrotHologram(DeathsPersistMap, "${CC.B_PRI}Top Deaths Leaderboard", location)