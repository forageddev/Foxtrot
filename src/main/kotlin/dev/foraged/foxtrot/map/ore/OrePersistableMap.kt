package dev.foraged.foxtrot.map.ore

import dev.foraged.commons.persist.impl.IntegerPersistMap
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.metadata.FixedMetadataValue

abstract class OrePersistableMap(
    keyPrefix: String,
    mongoName: String,
    private val color: String,
    vararg val types: Material
) : IntegerPersistMap(keyPrefix, mongoName, true), Listener
{
    val displayName: String get() {
        return color + mongoName.split(".")[1]
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) {
        if (event.block.type in types) event.block.setMetadata("PlacedByPlayer", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMine(event: BlockBreakEvent) {
        if (event.player.itemInHand != null && event.player.itemInHand.containsEnchantment(Enchantment.SILK_TOUCH) || event.block.hasMetadata("PlacedByPlayer")) return

        if (event.block.type in types) increment(event.player.uniqueId)
    }
}