package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemDamageEvent
import kotlin.random.Random

@Listeners
object QualityOfLifeListeners : Listener
{
    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: PlayerItemDamageEvent) {
        event.isCancelled = 30 < Random.nextInt(100)
    }
}