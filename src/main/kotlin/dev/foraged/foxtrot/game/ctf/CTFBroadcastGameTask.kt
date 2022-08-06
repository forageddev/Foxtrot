package dev.foraged.foxtrot.game.ctf

import dev.foraged.commons.annotations.runnables.Repeating
import dev.foraged.foxtrot.game.GameService
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.server.MapService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

@Repeating(30 * 20)
class CTFBroadcastGameTask : Runnable
{
    override fun run() {
        for (game in GameService.games.filterIsInstance<CTFGame>().filter { it.active }) {
            if (game.flagHolder != null) {
                Bukkit.broadcastMessage("")
                Bukkit.broadcastMessage("${CC.SEC}The flag is currently held by ${CC.PRI}${game.flagHolder!!.name}${CC.SEC}.")
                Bukkit.broadcastMessage("${CC.SEC}Stop them being able to capture it. ${CC.PRI}[${game.flagHolder!!.location.blockX}, ${game.flagHolder!!.location.blockY}, ${game.flagHolder!!.location.blockZ}]")
                Bukkit.broadcastMessage("")
            }
        }
    }
}