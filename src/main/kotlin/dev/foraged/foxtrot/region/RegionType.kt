package dev.foraged.foxtrot.region

import dev.foraged.foxtrot.map.cooldown.nopersist.SpawnTagMap
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.player.PlayerMoveEvent

enum class RegionType(val moveHandler: RegionMoveHandler)
{
    WARZONE(RegionMoveHandler.ALWAYS_TRUE),
    WILDNERNESS(RegionMoveHandler.ALWAYS_TRUE),
    ROAD(RegionMoveHandler.ALWAYS_TRUE),
    KOTH(RegionMoveHandler.PVP_TIMER),
    CITADEL(RegionMoveHandler.PVP_TIMER),
    CLAIMED_LAND(RegionMoveHandler.PVP_TIMER),
    SPAWN(
        object : RegionMoveHandler {
            override fun handleMove(event: PlayerMoveEvent): Boolean
            {
                if (SpawnTagMap.isOnCooldown(event.player.uniqueId) && event.player.gameMode != GameMode.CREATIVE)
                {
                    event.player.sendMessage(ChatColor.RED.toString() + "You cannot enter spawn while spawn-tagged.")
                    event.to = event.from
                    return false
                }
                if (!event.player.isDead)
                {
                    event.player.health = event.player.maxHealth
                    event.player.foodLevel = 20
                }
                return true
            }
        }
    );
}