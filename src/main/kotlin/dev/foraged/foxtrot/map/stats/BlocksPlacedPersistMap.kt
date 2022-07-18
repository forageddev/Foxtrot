package dev.foraged.foxtrot.map.stats

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.IntegerPersistMap
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

@RegisterMap
@Listeners
object BlocksPlacedPersistMap : IntegerPersistMap("BlocksPlaced", "Blocks_Placed", true, Bukkit.getServerName()), Listener
{
    @EventHandler
    fun onDeath(event: BlockPlaceEvent) {
        increment(event.player.uniqueId)
    }
}