package dev.foraged.foxtrot.map

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.BooleanPersistMap
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPickupItemEvent
import java.util.*

@RegisterMap
@Listeners
object CobblestonePersistMap : BooleanPersistMap("CobblePickups", "CobblePickup", false), Listener {
    @EventHandler
    fun onPlayerPickupItem(event: PlayerPickupItemEvent) {
        val player = event.player

        if (this[player.uniqueId] == false) event.isCancelled = true
    }

    fun toggle(uuid: UUID) : Boolean {
        val value = !(this[uuid] ?: false)
        updateValueAsync(uuid, value)
        return value
    }
}