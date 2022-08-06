package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.server.MapService
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import org.bukkit.Effect
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent

@Listeners
object StartOfTheWorldListener : Listener
{
    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return

        if (MapService.SOTW_ACTIVE && !MapService.SOTW_ENABLED.contains(event.entity.uniqueId)) {
            event.isCancelled = true
        }
    }


    @EventHandler
    fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        if (event.entity !is Player) return
        val damager = EventUtils.getAttacker(event.damager) ?: return

        if (MapService.SOTW_ACTIVE) {

            if (damager.uniqueId !in MapService.SOTW_ENABLED) {
                damager.sendMessage("${CC.RED}You cannot damage players as you have sotw protection enabled.")
                event.isCancelled = true
            }

            if (event.entity.uniqueId !in MapService.SOTW_ENABLED) {
                damager.sendMessage("${CC.RED}You cannot damage ${event.entity.name} as they have sotw protetion enabled.")
                event.isCancelled = true
            }

            if (event.isCancelled) event.entity.location.world.playEffect(event.entity.location, Effect.LAVA_POP, 5, 5)
        }
    }

    @EventHandler
    fun onFood(event: FoodLevelChangeEvent) {
        if (MapService.SOTW_ACTIVE) {
            event.foodLevel = 20
            event.isCancelled = true
        }
    }
}