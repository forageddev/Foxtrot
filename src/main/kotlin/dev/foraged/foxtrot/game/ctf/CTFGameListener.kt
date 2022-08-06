package dev.foraged.foxtrot.game.ctf

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.game.GameService
import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import org.apache.logging.log4j.core.config.plugins.util.PluginUtil
import org.bukkit.Bukkit
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

@Listeners
object CTFGameListener : Listener {

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        if (event.entity.type == EntityType.VILLAGER && event.entity.hasMetadata("GameFlag")) event.isCancelled = true
    }
    
    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (EventUtils.hasPlayerMoved(event)) {
            for (game in GameService.games.filterIsInstance<CTFGame>().filter { it.active }) {
                if (game.flagHolder != null && game.flagHolder!!.uniqueId == event.player.uniqueId) {
                    val location = game.holdingEntity!!.location
                    location.yaw = event.player.location.yaw
                    location.pitch = event.player.location.pitch

                    game.holdingEntity!!.teleport(location)
                    event.player.passenger = game.holdingEntity
                }
            }
        }
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (!event.player.isSneaking) return
        if (SpawnTagMap.isOnCooldown(event.player.uniqueId)) return

        val team = LandBoard.getTeam(event.player.location) ?: return
        if (team is PlayerTeam && team.isMember(event.player.uniqueId)) {
            for (game in GameService.games.filterIsInstance<CTFGame>().filter { it.active }) {
                if (game.flagHolder != null && game.flagHolder!!.uniqueId == event.player.uniqueId) {
                    game.stop(event.player)
                }
            }
        }
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        for (game in GameService.games.filterIsInstance<CTFGame>().filter { it.active }) {
            if (game.flagHolder != null && game.flagHolder!!.uniqueId == event.entity.uniqueId) {
                game.flagHolder = null
                event.entity.passenger = null

                Bukkit.broadcastMessage("")
                Bukkit.broadcastMessage("${CC.SEC}The flag has been dropped by ${CC.PRI}${event.entity.name}${CC.SEC}.")
                Bukkit.broadcastMessage("${CC.SEC}The flag can be picked up at. ${CC.PRI}[${game.flagHolder!!.location.blockX}, ${game.flagHolder!!.location.blockY}, ${game.flagHolder!!.location.blockZ}]")
                Bukkit.broadcastMessage("")
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        for (game in GameService.games.filterIsInstance<CTFGame>().filter { it.active }) {
            if (game.flagHolder != null && game.flagHolder!!.uniqueId == event.player.uniqueId) {
                game.flagHolder = null
                event.player.passenger = null

                Bukkit.broadcastMessage("")
                Bukkit.broadcastMessage("${CC.SEC}The flag has been dropped by ${CC.PRI}${event.player.name}${CC.SEC}.")
                Bukkit.broadcastMessage("${CC.SEC}The flag can be picked up at. ${CC.PRI}[${game.flagHolder!!.location.blockX}, ${game.flagHolder!!.location.blockY}, ${game.flagHolder!!.location.blockZ}]")
                Bukkit.broadcastMessage("")
            }
        }
    }
}