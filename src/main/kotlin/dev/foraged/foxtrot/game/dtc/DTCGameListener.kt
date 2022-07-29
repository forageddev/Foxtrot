package dev.foraged.foxtrot.game.dtc

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.game.GameService
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

@Listeners
object DTCGameListener : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onMine(event: BlockBreakEvent) {
        if (event.block.type == Material.OBSIDIAN) {
            GameService.games.filterIsInstance<DTCGame>().filter { it.active }.forEach {
                if (it.point.distance(event.block.location) < 3) {
                    event.isCancelled = true
                    it.trackedPoints[event.player.uniqueId] = it.trackedPoints.getOrDefault(event.player.uniqueId, 0).plus(1)
                    it.points--
                    if (it.points == 0) it.stop(event.player)
                }
            }
        }
    }
}