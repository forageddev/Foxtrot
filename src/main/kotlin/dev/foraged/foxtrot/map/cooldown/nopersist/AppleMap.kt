package dev.foraged.foxtrot.map.cooldown.nopersist

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.CooldownMap
import dev.foraged.commons.persist.RegisterMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.time.TimeUtil
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

@RegisterMap
@Listeners
object AppleMap : CooldownMap(15), Listener
{
    @EventHandler
    fun onConsume(event: PlayerItemConsumeEvent) {
        if (event.item.type != Material.GOLDEN_APPLE) return
        if (event.item.durability.toInt() != 0) return

        val player = event.player
        val cooldown = getCooldown(player.uniqueId)

        if (cooldown > System.currentTimeMillis()) {
            event.isCancelled = true
            player.sendMessage("${CC.RED}You cannot use this for another ${CC.BOLD}${TimeUtil.formatIntoDetailedString(((cooldown - System.currentTimeMillis()) / 1000).toInt())}")
            return
        }

        startCooldown(player.uniqueId)
    }
}