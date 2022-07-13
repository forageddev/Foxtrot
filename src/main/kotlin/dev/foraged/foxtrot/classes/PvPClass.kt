package dev.foraged.foxtrot.classes

import com.google.common.collect.HashBasedTable
import net.evilblock.cubed.util.bukkit.Constants
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionEffectExpireEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.PlayerInventory
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

abstract class PvPClass(val name: String, val warmup: Int, val consumables: List<Material>) : Listener
{
    companion object {
        val restores: HashBasedTable<UUID, PotionEffectType, PotionEffect> = HashBasedTable.create()

        fun removeInfiniteEffects(player: Player) {
            player.activePotionEffects.forEach {
                if (it.duration > 1000000) player.removePotionEffect(it.type)
            }
        }
    }

    val siteLink: String = "${Constants.SITE_LINK}/classes/${name.lowercase()}"

    open fun apply(player: Player) {}
    open fun tick(player: Player) {}
    open fun remove(player: Player) {}
    open fun canApply(player: Player) : Boolean {return true}
    open fun itemConsumed(player: Player, type: Material) : Boolean {return true}
    open fun getScoreboardLines(player: Player) : List<String> {return emptyList()}

    abstract fun qualifies(armor: PlayerInventory) : Boolean
    fun wearingAllArmor(armor: PlayerInventory) : Boolean {
        return (armor.helmet != null && armor.chestplate != null && armor.leggings != null && armor.boots != null)
    }

    class SavedPotion(var potionEffect: PotionEffect, var time: Long = 0, val perm: Boolean = false)

    open fun setRestoreEffect(player: Player, effect: PotionEffect)
    {
        var shouldCancel = true
        val activeList = player.activePotionEffects
        for (active in activeList)
        {
            if (active.type != effect.type) continue

            // If the current potion effect has a higher amplifier, ignore this one.
            if (effect.amplifier < active.amplifier)
            {
                return
            } else if (effect.amplifier == active.amplifier)
            {
                // If the current potion effect has a longer duration, ignore this one.
                if (0 < active.duration && (effect.duration <= active.duration || effect.duration - active.duration < 10))
                {
                    return
                }
            }
            restores.put(player.uniqueId, active.type, active)
            shouldCancel = false
            break
        }

        // Cancel the previous restore.
        player.addPotionEffect(effect, true)
        if (shouldCancel && effect.duration > 120 && effect.duration < 9600)
        {
            restores.remove(player.uniqueId, effect.type)
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player.itemInHand == null || !event.action.name.contains("RIGHT")) return

        if (PvPClassHandler.hasKitOn(event.player, this) && consumables.isNotEmpty() && consumables.contains(event.player.itemInHand.type)) {
            if (itemConsumed(event.player, event.item.type)) {
                if (event.player.itemInHand.amount > 1) event.player.itemInHand.amount = event.player.itemInHand.amount - 1
                else event.player.inventory.remove(event.player.itemInHand)
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    open fun onPotionEffectExpire(event: PotionEffectExpireEvent)
    {
        val livingEntity: LivingEntity = event.entity
        if (livingEntity is Player)
        {
            val previous = restores.remove(livingEntity.uniqueId, event.effect.type)
            if (previous != null && previous.duration < 1000000)
            {
                event.isCancelled = true
                livingEntity.addPotionEffect(previous, true)
                Bukkit.getLogger()
                    .info("Restored " + previous.type.toString() + " for " + livingEntity.name + ". duration: " + previous.duration + ". amp: " + previous.amplifier)
            }
        }
    }
}