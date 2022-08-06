package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerPickupItemEvent

@Listeners
object ProtectionListener : Listener {
    private val droppedItems: MutableSet<Int> = HashSet()

    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        event.blockList().clear()
    }

    @EventHandler
    fun onPlayerPickupItem(event: PlayerPickupItemEvent) {
        if (PvPTimerPersistableMap.isOnCooldown(event.player.uniqueId)) {
            event.isCancelled = droppedItems.contains(event.item.entityId)
        }
    }

    @EventHandler
    fun onItemSpawn(event: ItemSpawnEvent)
    {
        val itemStack = event.entity.itemStack
        if (itemStack.hasItemMeta() && itemStack.itemMeta.hasLore() && itemStack.itemMeta.lore.contains("§8PVP Loot")) {
            val meta = itemStack.itemMeta
            val lore = meta.lore
            lore.remove("§8PVP Loot")
            meta.lore = lore
            itemStack.itemMeta = meta
            event.entity.itemStack = itemStack
            val id = event.entity.entityId
            droppedItems.add(id)

            Tasks.delayed(20 * 60) {
                droppedItems.remove(id)
            }
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        for (itemStack in event.drops) {
            val meta = itemStack.itemMeta
            val lore = meta.lore ?: mutableListOf()
            lore.add("§8PVP Loot")
            meta.lore = lore
            itemStack.itemMeta = meta
        }
    }

    @EventHandler
    fun onEntityShootBow(event: EntityShootBowEvent)
    {
        if (event.entity is Player) {
            val player = event.entity as Player
            if (PvPTimerPersistableMap.isOnCooldown(player.uniqueId)) {
                player.sendMessage(ChatColor.RED.toString() + "You cannot do this while your PVP Timer is active!")
                player.sendMessage(ChatColor.RED.toString() + "Type '" + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + "' to remove your timer.")
                event.isCancelled = true
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent)
    {
        if (event.entity !is Player) return

        val damager: Player = EventUtils.getAttacker(event.damager) ?: return // find the player damager if one exists
        if (PvPTimerPersistableMap.isOnCooldown(damager.uniqueId)) {
            damager.sendMessage(ChatColor.RED.toString() + "You cannot do this while your PVP Timer is active!")
            damager.sendMessage(ChatColor.RED.toString() + "Type '" + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + "' to remove your timer.")
            event.isCancelled = true
            return
        }
        if (PvPTimerPersistableMap.isOnCooldown(event.entity.uniqueId)) {
            damager.sendMessage(ChatColor.RED.toString() + "That player currently has their PVP Timer!")
            event.isCancelled = true
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEntityDamage(event: EntityDamageEvent)
    {
        if (event.entity is Player && (event.cause == EntityDamageEvent.DamageCause.LAVA || event.cause == EntityDamageEvent.DamageCause.FIRE || event.cause == EntityDamageEvent.DamageCause.FIRE_TICK)) {
            val player = event.entity as Player
            event.isCancelled = PvPTimerPersistableMap.isOnCooldown(player.uniqueId)
        }
    }
}