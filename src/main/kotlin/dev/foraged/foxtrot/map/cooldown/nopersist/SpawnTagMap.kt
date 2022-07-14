package dev.foraged.foxtrot.map.cooldown.nopersist

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.CooldownMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.*

@Listeners
object SpawnTagMap : CooldownMap(30), Listener
{
    override fun startCooldown(uuid: UUID, seconds: Int) {
        if (!isOnCooldown(uuid)) Bukkit.getPlayer(uuid).sendMessage("${CC.SEC}You have been spawn-tagged for ${CC.RED}${seconds}${CC.YELLOW} seconds!")

        super.startCooldown(uuid, seconds)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        resetCooldown(event.entity.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent)
    {
        if (event.entity !is Player) return

        val damager = EventUtils.getAttacker(event.damager) ?: return

        if (damager !== event.entity) {
            startCooldown(damager.uniqueId)
            startCooldown(event.entity.uniqueId)
        }
    }
}