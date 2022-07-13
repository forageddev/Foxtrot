package dev.foraged.foxtrot.map

import com.google.common.collect.ImmutableSet
import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.BooleanPersistMap
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.metadata.FixedMetadataValue
import java.util.*

@RegisterMap
@Listeners
object FoundDiamondsPersistMap : BooleanPersistMap("FoundDiamonds", "FoundDiamondsEnabled", false), Listener {

    val CHECK_FACES: Set<BlockFace> = ImmutableSet.of(
        BlockFace.NORTH,
        BlockFace.SOUTH,
        BlockFace.EAST,
        BlockFace.WEST,
        BlockFace.NORTH_EAST,
        BlockFace.NORTH_WEST,
        BlockFace.SOUTH_EAST,
        BlockFace.SOUTH_WEST,
        BlockFace.UP,
        BlockFace.DOWN
    )

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (event.block.type == Material.DIAMOND_ORE) {
            event.block.setMetadata("DiamondPlaced", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (event.block.type == Material.DIAMOND_ORE && !event.block.hasMetadata("DiamondPlaced"))
        {
            val diamonds = countRelative(event.block)
            Bukkit.getServer().onlinePlayers.forEach {
                if (this[it.uniqueId] == true) {
                    it.sendMessage("[FD] ${CC.AQUA}${event.player.name} found $diamonds${TextUtil.pluralize(diamonds, "diamond"," diamonds")}.")
                }
            }
        }
    }

    fun countRelative(block: Block): Int
    {
        var diamonds = 1 // We start out with one because 'block' is going to be a diamond too.
        block.setMetadata("DiamondPlaced", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))
        for (checkFace in CHECK_FACES)
        {
            val relative = block.getRelative(checkFace)
            if (relative.type == Material.DIAMOND_ORE && !relative.hasMetadata("DiamondPlaced"))
            {
                relative.setMetadata("DiamondPlaced", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))
                diamonds += countRelative(relative)
            }
        }
        return diamonds
    }

    fun toggle(uuid: UUID) : Boolean {
        val value = !(this[uuid] ?: false)
        updateValueAsync(uuid, value)
        return value
    }
}