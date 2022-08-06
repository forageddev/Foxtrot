package dev.foraged.foxtrot.team.dtr

import dev.foraged.commons.annotations.runnables.Repeating
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.TeamService
import dev.foraged.foxtrot.team.impl.PlayerTeam
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.*

@Repeating(20L)
class RegenerationTask : Runnable {

    companion object {
        val BASE_DTR_INCREMENT = doubleArrayOf(
            1.5, .5, .45, .4, .36,
            .33, .3, .27, .24, .22, .21, .2, .19, .18, .175, .17, .168, .166,
            .164, .162, .16, .158, .156, .154, .152, .15, .148, .146, .144,
            .142, .142, .142, .142, .142, .142,
            .142, .142, .142, .142, .142
        )
        var MAX_DTR = doubleArrayOf(
            1.01, 2.01, 3.25, 3.75, 4.50,  // 1 to 5
            5.25, 5.50, 5.50, 5.50,  // 6 to 10
            5.50, 5.50, 5.50, 5.80, 6.05,  // 11 to 15
            6.15, 6.25, 6.35, 6.45, 6.55,  // 16 to 20
            6.65, 6.7, 6.85, 6.95, 7.00,  // 21 to 25
            7.0, 7.0, 7.0, 7.0, 7.0,  // 26 to 30
            7.0, 7.0, 7.0, 7.0, 7.0, 9.0, 9.0, 9.0, 9.0, 9.0
        ) // Padding


        private val wasOnCooldown = mutableSetOf<UUID>()

        // * 4.5 is to 'speed up' DTR regen while keeping the ratios the same.
        // We're using this instead of changing the array incase we need to change this value
        // In the future.
        fun getBaseDTRIncrement(teamsize: Int): Double {
            return if (teamsize == 0) 0.0 else BASE_DTR_INCREMENT[teamsize - 1] * 4.5
        }

        fun getMaxDTR(teamsize: Int): Double
        {
            return if (teamsize == 0) 100.0 else MAX_DTR[teamsize - 1]
        }

        fun isOnCooldown(team: PlayerTeam): Boolean
        {
            if (MapService.EOTW_ACTIVE) return true
            return team.regenTime > System.currentTimeMillis()
        }

        fun isRegenerating(team: PlayerTeam): Boolean
        {
            if (MapService.EOTW_ACTIVE) return false
            return !isOnCooldown(team) && team.deathsUntilRaidable != team.maxDeathsUntilRaidable
        }

        fun markOnDTRCooldown(team: PlayerTeam)
        {
            wasOnCooldown.add(team.identifier)
        }
    }

    override fun run() {
        val playerOnlineMap = mutableMapOf<PlayerTeam, Int>()
        for (player in Bukkit.getServer().onlinePlayers)
        {
            if (player.hasMetadata("invisible")) continue

            val playerTeam = TeamService.findTeamByPlayer(player.uniqueId)
            if (playerTeam != null) playerOnlineMap[playerTeam] = playerOnlineMap.getOrDefault(playerTeam, 0) + 1

        }
        playerOnlineMap.forEach { team, onlineCount ->
            try
            {
                // make sure (I guess?)
                if (isOnCooldown(team))
                {
                    markOnDTRCooldown(team)
                    return@forEach
                }

                if (wasOnCooldown.remove(team.identifier))
                {
                    team.broadcast("${CC.B_GREEN}Your team is now regenerating DTR!")
                }
                val incrementedDtr = team.deathsUntilRaidable + team.getDTRIncrement(onlineCount)
                val maxDtr: Double = team.maxDeathsUntilRaidable
                val newDtr = incrementedDtr.coerceAtMost(maxDtr)
                team.deathsUntilRaidable = newDtr
            } catch (ex: Exception)
            {
                FoxtrotExtendedPlugin.instance.logger.warning("Error regenerating DTR for team " + team.name + ".")
                ex.printStackTrace()
            }
        }
    }
}