package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.region.RegionData
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.claim.LandBoard
import dev.foraged.foxtrot.team.enums.SystemFlag
import dev.foraged.foxtrot.team.impl.PlayerTeam
import dev.foraged.foxtrot.team.impl.SystemTeam
import mkremins.fanciful.FancyMessage
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.bukkit.EventUtils
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent

@Listeners
object TraverseListener : Listener
{
    @EventHandler
    fun onTeleport(event: PlayerTeleportEvent) {
        processTerritoryInfo(event)
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        if (EventUtils.hasPlayerMoved(event)) processTerritoryInfo(event)
    }

    private fun processTerritoryInfo(event: PlayerMoveEvent)
    {
        val ownerTo = LandBoard.getTeam(event.to)
        if (PvPTimerPersistableMap[event.player.uniqueId] != null)
        {
            if (!SystemFlag.SAFE_ZONE.appliesAt(event.to))
            {
                if (SystemFlag.KING_OF_THE_HILL.appliesAt(event.to) || SystemFlag.CITADEL.appliesAt(event.to) && PvPTimerPersistableMap.isOnCooldown(event.player.uniqueId))
                {
                    PvPTimerPersistableMap.startCooldown(event.player.uniqueId, 0)
                    event.player.sendMessage(CC.RED.toString() + "Your PvP Protection has been removed for entering claimed land.")
                } else if (ownerTo != null && ownerTo is PlayerTeam) {
                    if (!ownerTo.isMember(event.player.uniqueId))
                    {
                        event.isCancelled = true
                        for (claim in ownerTo.claims)
                        {
                            // TODO: sort this shit out later todo: soprt it
                            /*if (claim.contains(event.from) && !ownerTo.isMember(event.player.uniqueId))
                            {
                                var nearest: Location = TeamStuckCommand.nearestSafeLocation(event.player.location)
                                var spawn = false
                                if (nearest == null)
                                {
                                    nearest = Bukkit.getServer().getWorld("world").spawnLocation
                                    spawn = true
                                }
                                event.player.teleport(nearest)
                                event.player.sendMessage("${CC.RED}Moved you to " + (if (spawn) "spawn" else "nearest unclaimed territory") + " because you were in land that was claimed.")
                                return
                            }*/
                        }
                        event.player.sendMessage("${CC.RED}You cannot enter another team's territory with PvP Protection.")
                        event.player.sendMessage("${CC.RED}Use ${CC.YELLOW}/pvp enable${CC.RED} to remove your protection.")
                        return
                    }
                }
            }
        }
        val ownerFrom = LandBoard.getTeam(event.from)
        if (ownerFrom !== ownerTo)
        {
            val from: RegionData = MapService.getRegion(ownerFrom, event.from)
            val to: RegionData = MapService.getRegion(ownerTo, event.to)
            if (from == to) return
            if (!to.regionType.moveHandler.handleMove(event))
            {
                return
            }
            var fromReduceDeathban = from.data != null && from.data is SystemTeam && (from.data
                .hasFlag(SystemFlag.FIVE_MINUTE_DEATHBAN) || from.data
                .hasFlag(SystemFlag.FIFTEEN_MINUTE_DEATHBAN) || from.data
                .hasFlag(SystemFlag.SAFE_ZONE))
            var toReduceDeathban =
                to.data != null && to.data is SystemTeam && (to.data.hasFlag(SystemFlag.FIVE_MINUTE_DEATHBAN) || to.data
                    .hasFlag(SystemFlag.FIFTEEN_MINUTE_DEATHBAN) || to.data
                    .hasFlag(SystemFlag.SAFE_ZONE))
/*
            if (fromReduceDeathban && from.data != null)
            {
                val fromLinkedKOTH: Event = Foxtrot.getInstance().getEventHandler().getEvent(from.data.getName())
                if (fromLinkedKOTH != null && !fromLinkedKOTH.isActive())
                {
                    fromReduceDeathban = false
                }
            }
            if (toReduceDeathban && to.data != null)
            {
                val toLinkedKOTH: Event = Foxtrot.getInstance().getEventHandler().getEvent(to.data.getName())
                if (toLinkedKOTH != null && !toLinkedKOTH.isActive())
                {
                    toReduceDeathban = false
                }
            }
*/

            // create leaving message
            val nowLeaving: FancyMessage =
                FancyMessage("Now leaving: ").color(ChatColor.YELLOW).then(from.getName(event.player)).color(
                    ChatColor.YELLOW
                )
            if (ownerFrom != null)
            {
                nowLeaving.command("/t i " + ownerFrom.name).tooltip(CC.GREEN.toString() + "View team info")
            }
            nowLeaving.then(" (").color(ChatColor.YELLOW).then(if (fromReduceDeathban) "Non-Deathban" else "Deathban")
                .color(if (fromReduceDeathban) ChatColor.GREEN else ChatColor.RED).then(")").color(
                    ChatColor.YELLOW
                )

            // create entering message
            val nowEntering: FancyMessage =
                FancyMessage("Now entering: ").color(ChatColor.YELLOW).then(to.getName(event.player)).color(
                    ChatColor.YELLOW
                )
            if (ownerTo != null)
            {
                nowEntering.command("/t i " + ownerTo.name).tooltip(CC.GREEN.toString() + "View team info")
            }
            nowEntering.then(" (").color(ChatColor.YELLOW).then(if (toReduceDeathban) "Non-Deathban" else "Deathban")
                .color(if (toReduceDeathban) ChatColor.GREEN else ChatColor.RED).then(")").color(
                    ChatColor.YELLOW
                )

            // send both
            nowLeaving.send(event.player)
            nowEntering.send(event.player)
        }
    }
}