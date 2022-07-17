package dev.foraged.foxtrot.ability.impl

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.ability.Ability
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.map.cooldown.nopersist.AbilityCooldownMap
import dev.foraged.foxtrot.team.enums.SystemFlag
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.player.PlayerInteractEvent

@Listeners
object SwitcherAbility : Ability("switcher", "Switcher", ChatColor.AQUA, Material.SNOW_BALL, listOf(
    "${CC.YELLOW}Switch places with your enemies",
    "${CC.YELLOW}to land them in unfortunate positions."
), 30)
{
    @EventHandler
    fun onLaunch(event: PlayerInteractEvent) {
        val player = event.player
        if (isAbilityItem(player.itemInHand)) {
            event.isCancelled = true
            if (AbilityCooldownMap.isOnCooldown(player.uniqueId)) {
                player.sendMessage("${CC.RED}You cannot use ability items for another ${TimeUtil.formatIntoDetailedString(((AbilityCooldownMap.getCooldown(player.uniqueId) - System.currentTimeMillis()) / 1000).toInt())}.")
                player.updateInventory()
                return
            }

            if (isOnCooldown(player.uniqueId)) {
                player.sendMessage("${CC.RED}You cannot use a $id for another ${TimeUtil.formatIntoDetailedString(((getCooldown(player.uniqueId) - System.currentTimeMillis()) / 1000).toInt())}.")
                player.updateInventory()
                return
            }

            if (player.itemInHand.amount == 1) player.itemInHand = null
            else player.itemInHand.amount--

            player.launchProjectile(Snowball::class.java)
            startCooldown(player.uniqueId)
        }
    }

    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager is Snowball) {
            val damager = (event.damager as Snowball).shooter as Player
            val location = damager.location.clone()

            if (PvPTimerPersistableMap.isOnCooldown(event.entity.uniqueId)) {
                damager.sendMessage("${CC.RED}You cannot switcher players who have pvp protection.")
                return
            }

            if (SystemFlag.SAFE_ZONE.appliesAt(event.entity.location)) {
                damager.sendMessage("${CC.RED}You cannot switcher players who are in spawn.")
                return
            }

            if (SystemFlag.SAFE_ZONE.appliesAt(location)) {
                damager.sendMessage("${CC.RED}You cannot use switchers in spawn.")
                return
            }

            if (SystemFlag.KING_OF_THE_HILL.appliesAt(event.entity.location)) {
                damager.sendMessage("${CC.RED}You cannot switcher players in event zones.")
                return
            }

            damager.teleport(event.entity.location)
            event.entity.teleport(location)

            damager.sendMessage("${CC.SEC}You have switched places with ${CC.PRI}${event.entity.name}${CC.SEC}.")
            event.entity.sendMessage("${CC.SEC}You have switched places with ${CC.PRI}${damager.name}${CC.SEC}.")
        }
    }
}