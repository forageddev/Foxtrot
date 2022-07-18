package dev.foraged.foxtrot.map.stats

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.IntegerPersistMap
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

@RegisterMap
@Listeners
object BlocksMinedPersistMap : IntegerPersistMap("BlocksMined", "Blocks_Mined", true, Bukkit.getServerName()), Listener
{
    @EventHandler
    fun onBreak(event: BlockBreakEvent) {
        increment(event.player.uniqueId)
    }
}