package dev.foraged.foxtrot.hologram.impl

import dev.foraged.foxtrot.hologram.FoxtrotHologram
import dev.foraged.foxtrot.map.stats.KillsPersistMap
import net.evilblock.cubed.util.CC
import org.bukkit.Location

class KillsHologram(location: Location) : FoxtrotHologram(KillsPersistMap, "${CC.B_PRI}Top Kills Leaderboard", location)