package dev.foraged.foxtrot.game.ktk

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.game.GameService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

@Listeners
object KTKListener : Listener
{
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        for (game in GameService.games.filterIsInstance<KTKGame>().filter { it.active }) {
            if (game.king != null && game.king.uniqueId == event.entity.uniqueId) {
                game.stop(game.king.killer)
            }
        }
    }
}