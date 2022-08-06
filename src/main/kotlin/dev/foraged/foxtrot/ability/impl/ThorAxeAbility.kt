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
object ThorAxeAbility : Ability("thoraxe", "Thor Axe", ChatColor.AQUA, Material.IRON_AXE, listOf(
    "${CC.YELLOW}Strike your enemies with the force",
    "${CC.YELLOW}of the lightning and thunder."
), 30)
{
    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        if (event.damager !is Player) return

        val player = event.damager as Player
        if (event.isCancelled) return
        if (isAbilityItem(player.itemInHand)) {
            event.isCancelled = true
            if (!checkCooldown(player)) return


            event.entity.world.strikeLightning(event.entity.location)
            (event.entity as Player).damage(2.5, player)
            player.sendMessage("${CC.SEC}You have smitten ${CC.PRI}${event.entity.name}${CC.SEC}.")
            startCooldown(player.uniqueId)
        }
    }
}