package dev.foraged.foxtrot.game.dtc

import dev.foraged.commons.annotations.runnables.Repeating
import dev.foraged.foxtrot.game.GameService
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.server.MapService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit
import java.util.concurrent.TimeUnit

@Repeating(2)
class DTCGameTask : Runnable
{
    override fun run() {
        for (game in GameService.games.filterIsInstance<DTCGame>().filter { it.active }) {
            if (System.currentTimeMillis() > game.nextRegeneration && game.points < 100) {
                game.points++
                game.nextRegeneration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)
                Bukkit.broadcastMessage("${DTCGame.CHAT_PREFIX}${CC.SEC}The game ${CC.PRI}${game.name}${CC.SEC} is regenerating. ${CC.GRAY}(${game.points})")
            }
        }
    }
}