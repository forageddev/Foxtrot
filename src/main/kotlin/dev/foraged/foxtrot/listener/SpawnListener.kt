package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.enums.SystemFlag
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Horse
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockIgniteEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEntityEvent

@Listeners
object SpawnListener : Listener
{
    @EventHandler(priority = EventPriority.HIGH)
    fun onBlockIgnite(event: BlockIgniteEvent)
    {
        if (event.player != null) if (MapService.isAdminOverride(event.player)) return

        event.isCancelled = SystemFlag.SAFE_ZONE.appliesAt(event.block.location)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent)
    {
        if (MapService.isAdminOverride(event.player)) return

        if (SystemFlag.SAFE_ZONE.appliesAt(event.block.location)) {
            event.isCancelled = true
            event.player.sendMessage(ChatColor.YELLOW.toString() + "You cannot build in spawn!")
        } else if (MapService.isSpawnBufferZone(event.block.location) || MapService.isNetherBufferZone(event.block.location)) {
            if (!SystemFlag.SAFE_ZONE.appliesAt(event.block.location) && event.itemInHand != null && event.itemInHand.type == Material.WEB && MapService.KIT_MAP) {
                if (!SystemFlag.ALLOW_COBWEBS.appliesAt(event.block.location)) {
                    event.player.sendMessage("${CC.RED}Cobwebs cannot be used in this region.")
                    return
                }

                Tasks.delayed(10 * 20L) {
                    if (event.block.type == Material.WEB) event.block.type = Material.AIR
                }
            } else {
                event.isCancelled = true
                event.player.sendMessage(ChatColor.YELLOW.toString() + "You cannot build this close to spawn!")
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent)
    {
        if (MapService.isAdminOverride(event.player)) return

        if (SystemFlag.SAFE_ZONE.appliesAt(event.block.location)) {
            event.isCancelled = true
            event.player.sendMessage(ChatColor.YELLOW.toString() + "You cannot build in spawn!")
        } else if (!SystemFlag.DTC.appliesAt(event.block.location) && (MapService
                .isSpawnBufferZone(event.block.location) || MapService.isNetherBufferZone(event.block.location)))
        {
            event.isCancelled = true
            if (event.block.type != Material.LONG_GRASS && event.block.type != Material.GRASS) event.player.sendMessage(ChatColor.YELLOW.toString() + "You cannot build this close to spawn!")

        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onHangingPlace(event: HangingPlaceEvent) {
        if (MapService.isAdminOverride(event.player)) return
        event.isCancelled = SystemFlag.SAFE_ZONE.appliesAt(event.entity.location)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onHangingBreakByEntity(event: HangingBreakByEntityEvent) {
        if (event.remover !is Player || MapService.isAdminOverride(event.remover as Player)) return
        event.isCancelled = SystemFlag.SAFE_ZONE.appliesAt(event.entity.location)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerInteractEntityEvent(event: PlayerInteractEntityEvent) {
        if (event.rightClicked.type != EntityType.ITEM_FRAME || MapService.isAdminOverride(event.player)) return
        event.isCancelled = SystemFlag.SAFE_ZONE.appliesAt(event.rightClicked.location)

    }

    // Used for item frames
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player || event.entity.type != EntityType.ITEM_FRAME || MapService.isAdminOverride(event.damager as Player)) return
        event.isCancelled = SystemFlag.SAFE_ZONE.appliesAt(event.entity.location)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: FoodLevelChangeEvent) {
        if (SystemFlag.SAFE_ZONE.appliesAt(event.entity.location)) {
            (event.entity as Player).foodLevel = 20
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent) {
        if ((event.entity is Player || event.entity is Horse) && SystemFlag.SAFE_ZONE.appliesAt(event.entity.location)) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamageByEntity2(event: EntityDamageByEntityEvent)
    {
        if (event.entity !is Player) return

        val damager = EventUtils.getAttacker(event.damager) // find the player damager if one exists
        if (damager != null) {
            val victim = event.entity as Player
            if (SystemFlag.SAFE_ZONE.appliesAt(victim.location) || SystemFlag.SAFE_ZONE.appliesAt(damager.location)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onDrop(event: PlayerDropItemEvent) {
        if (SystemFlag.SAFE_ZONE.appliesAt(event.player.location) && MapService.KIT_MAP || MapService.SOTW_ACTIVE) {
            event.itemDrop.remove()
        }
    }
}