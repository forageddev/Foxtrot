package dev.foraged.foxtrot.region

import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.event.player.PlayerMoveEvent

interface RegionMoveHandler
{
    fun handleMove(event: PlayerMoveEvent): Boolean

    companion object
    {
        val ALWAYS_TRUE: RegionMoveHandler = object : RegionMoveHandler
        {
            override fun handleMove(event: PlayerMoveEvent): Boolean
            {
                return true
            }
        }
        val PVP_TIMER: RegionMoveHandler = object : RegionMoveHandler
        {
            override fun handleMove(event: PlayerMoveEvent): Boolean
            {
                if (PvPTimerPersistableMap.isOnCooldown(event.player.uniqueId) && event.player.gameMode != GameMode.CREATIVE)
                {
                    event.player.sendMessage(ChatColor.RED.toString() + "You cannot do this while your PVP Timer is active!")
                    event.player.sendMessage(ChatColor.RED.toString() + "Type '" + ChatColor.YELLOW + "/pvp enable" + ChatColor.RED + "' to remove your timer.")
                    event.to = event.from
                    return false
                }
                return true
            }
        }
    }
}