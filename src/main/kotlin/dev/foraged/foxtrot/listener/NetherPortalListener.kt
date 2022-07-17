package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.enums.SystemFlag
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent

@Listeners
object NetherPortalListener : Listener
{
    @EventHandler
    fun onPlayerPortal(event: PlayerPortalEvent) {
        val player = event.player
        if (event.cause != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) return

        if (event.to.world.environment == World.Environment.NORMAL) {
            if (SystemFlag.SAFE_ZONE.appliesAt(event.from)) {
                event.isCancelled = true
                player.teleport(Bukkit.getServer().getWorld("world").spawnLocation)
                player.sendMessage("${CC.GREEN}Teleported to overworld spawn!")
            }
        }

        val to = event.to
        if (SystemFlag.ROAD.appliesAt(to)) {
            val team = LandBoard.getTeam(to)!!
            if (team.name == "North") to.add(20.0, 0.0, 0.0) // add 20 on the X axis
            else if (team.name == "South") to.subtract(20.0, 0.0, 0.0) // subtract 20 on the X axis
            else if (team.name == "East") to.add(0.0, 0.0, 20.0) // add 20 on the Z axis
            else if (team.name == "West") to.subtract(0.0, 0.0, 20.0) // subtract 20 on the Z axis
        }
        event.to = to
    }
}