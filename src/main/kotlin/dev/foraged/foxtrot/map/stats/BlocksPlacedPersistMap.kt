package dev.foraged.foxtrot.map.stats

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.IntegerPersistMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent

@RegisterMap
@Listeners
object BlocksPlacedPersistMap : IntegerPersistMap("BlocksPlaced", "BlocksPlaced", true), Listener
{
    @EventHandler
    fun onDeath(event: BlockPlaceEvent) {
        increment(event.player.uniqueId)
    }
}