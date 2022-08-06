package dev.foraged.foxtrot.game.koth

import dev.foraged.commons.annotations.runnables.Repeating
import dev.foraged.foxtrot.game.GameService
import dev.foraged.foxtrot.map.cooldown.PvPTimerPersistableMap
import dev.foraged.foxtrot.server.MapService
import net.evilblock.cubed.util.CC
import org.bukkit.Bukkit

@Repeating(2)
class KothGameTask : Runnable
{
    override fun run() {
        for (game in GameService.games.filterIsInstance<KothGame>().filter { it.active }) {
            if (game.finished) {
                game.stop(null)
                continue
            }

            if (game.controllingPlayer != null) {
                if (game.controllingPlayer!!.isDead || !game.controllingPlayer!!.isOnline || !game.captureZone.contains(game.controllingPlayer!!)) {
                    game.controllingPlayer = null
                } else if ((game.remainingMillis / 100) % 600 == 0L && game.remainingMillis != game.captureTime && game.remainingMillis != 0L && !game.formatTimeRemaining().contains("0")) {
                    Bukkit.broadcastMessage("${KothGame.CHAT_PREFIX}${CC.SEC}The game ${CC.PRI}${game.name}${CC.SEC} is currently being contested. ${CC.GRAY}(${game.formatTimeRemaining()})")
                }

            } else if (!game.grace) {
                for (player in Bukkit.getServer().onlinePlayers.filterNot { MapService.isAdminOverride(it) }.filterNot { it.isDead }) {
                    if (PvPTimerPersistableMap.isOnCooldown(player.uniqueId)) continue

                    if (game.captureZone.contains(player)) {
                        game.controllingPlayer = player
                        break
                    }
                }
            }
        }
    }
}