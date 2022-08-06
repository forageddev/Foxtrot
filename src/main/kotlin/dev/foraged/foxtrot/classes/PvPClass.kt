package dev.foraged.foxtrot.classes

import com.google.common.collect.HashBasedTable
import dev.foraged.commons.persist.CooldownMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.Constants
import net.evilblock.cubed.util.text.ProgressBarBuilder
import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
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
import kotlin.random.Random

abstract class PvPClass(val name: String, val color: ChatColor, val warmup: Int, val consumables: List<Material>) : Listener
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
    val ultimatePercentages = mutableMapOf<UUID, Double>()
    val ultimateActivated = mutableListOf<UUID>()

    open fun apply(player: Player) {}
    open fun tick(player: Player) {}
    open fun remove(player: Player) {}
    open fun canApply(player: Player) : Boolean = true
    open fun itemConsumed(player: Player, type: Material) : Boolean = true
    open fun getScoreboardLines(player: Player) : List<String> {
        return listOf(
            "${color.toString() + CC.B}Class",
            " ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT}${CC.WHITE} Type: $color$name",
            " ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT}${CC.WHITE} Ultimate: $color${
                TextUtil.colorPercentage(ultimatePercentages[player.uniqueId] ?: 0.0)
            }${(ultimatePercentages[player.uniqueId] ?: 0.0).toString().split(".")[0]}%")
    }

    open fun activateUltimate(player: Player) {
        ultimatePercentages[player.uniqueId] = 0.0
        ultimateActivated.add(player.uniqueId)
    }

    open fun isUltimateActive(player: Player) : Boolean = ultimateActivated.contains(player.uniqueId)
    open fun deactivateUltimate(player: Player) = ultimateActivated.remove(player.uniqueId)
    open fun increaseUltimate(player: Player, amount: Double = Random.nextInt(3, 8).toDouble()) {
        var asf = (ultimatePercentages[player.uniqueId] ?: 0.0) + amount
        if (asf > 100) asf = 100.0

        ultimatePercentages[player.uniqueId] = asf
    }

    open fun isUltimateReady(player: Player) : Boolean =  (ultimatePercentages[player.uniqueId] ?: 0.0) >= 100.0

    open fun getActionBarText(player: Player) : String {
        return "${CC.B_AQUA}$name Ultimate ${CC.GRAY}${Constants.DOUBLE_ARROW_RIGHT} ${CC.RED}${ProgressBarBuilder.DEFAULT.build(ultimatePercentages[player.uniqueId] ?: 0.0)}"
    }

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

        if (PvPClassService.hasKitOn(event.player, this) && consumables.isNotEmpty() && consumables.contains(event.player.itemInHand.type)) {
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