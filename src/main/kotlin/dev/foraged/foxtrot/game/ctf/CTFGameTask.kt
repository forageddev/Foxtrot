package dev.foraged.foxtrot.game.ctf

import dev.foraged.commons.annotations.runnables.Repeating
import dev.foraged.foxtrot.game.GameService
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.server.MapService
import dev.foraged.foxtrot.team.TeamService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

@Repeating(2)
class CTFGameTask : Runnable
{
    override fun run() {
        for (game in GameService.games.filterIsInstance<CTFGame>().filter { it.active }) {
            if (game.flagHolder == null) {
                Bukkit.getServer().onlinePlayers.filter { it.isSneaking }.filter { it.location.distance(game.holdingEntity!!.location) < 4 }.forEach {
                    val start = it.getMetadata("StartedSneaking")[0].asLong()

                    if (System.currentTimeMillis() > (start + TimeUnit.SECONDS.toMillis(10))) {
                        val team = TeamService.findTeamByPlayer(it.uniqueId)
                        if (team == null) {
                            it.sendMessage("${CC.RED}You cannot capture the flag whilst you are not in a team.")
                            return@forEach
                        }

                        game.flagHolder = it
                        it.passenger = game.holdingEntity
                        Bukkit.broadcastMessage("")
                        Bukkit.broadcastMessage("${CC.B_PRI}The flag ${it.name} has been picked up!")
                        Bukkit.broadcastMessage("${CC.SEC}The flag holder is ${CC.PRI}${it.name}${CC.SEC}.")
                        Bukkit.broadcastMessage("")
                    }
                }
            }
        }
    }
}