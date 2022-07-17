package dev.foraged.foxtrot.map.cooldown.nopersist

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.CooldownMap
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import net.evilblock.cubed.util.bukkit.Tasks
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitTask
import java.util.*

@RegisterMap
@Listeners
object LogoutMap : CooldownMap(30), Listener {

    val resetMap = mutableSetOf<UUID>()
    val taskTrack = mutableMapOf<UUID, BukkitTask>()

    override fun startCooldown(uuid: UUID, seconds: Int)
    {
        super.startCooldown(uuid, seconds)

        Bukkit.getPlayer(uuid).setMetadata("safeLogout", FixedMetadataValue(FoxtrotExtendedPlugin.instance, true))
        resetMap.remove(uuid)
        taskTrack[uuid] = Tasks.delayed(seconds * 20L) {
            if (resetMap.contains(uuid)) {
                resetMap.remove(uuid)
                return@delayed
            }

            val player = Bukkit.getPlayer(uuid) ?: return@delayed
            player.kickPlayer("${CC.RED}You have safely logged out of the server.")
        }
    }

    override fun resetCooldown(uuid: UUID)
    {
        resetMap.add(uuid)
        taskTrack[uuid]?.cancel()
        taskTrack.remove(uuid)
        super.resetCooldown(uuid)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity !is Player) return

        if (!isOnCooldown(event.entity.uniqueId)) return

        resetCooldown(event.entity.uniqueId)
        event.entity.sendMessage("${CC.RED}Your logout has been cancelled.")
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (EventUtils.hasPlayerMoved(event)) {
            if (!isOnCooldown(event.player.uniqueId)) return

            resetCooldown(event.player.uniqueId)
            event.player.sendMessage("${CC.RED}Your logout has been cancelled.")
        }
    }
}
