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
object SwitchStickAbility : Ability("switchstick", "Switch Stick", ChatColor.GOLD, Material.STICK, listOf(
    "${CC.YELLOW}Spin your enemies around with a",
    "${CC.YELLOW}simple click of a stick."
), 30)
{
    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager !is Player) return

        val player = event.damager as Player
        if (event.isCancelled) return
        if (isAbilityItem(player.itemInHand)) {
            if (!checkCooldown(player)) return

            val location = event.entity.location
            location.yaw = event.entity.location.yaw + 180
            event.entity.teleport(location)
            player.sendMessage("${CC.SEC}You have rotated ${CC.PRI}${event.entity.name}${CC.SEC} 180 degrees.")
            startCooldown(player.uniqueId)
        }
    }
}