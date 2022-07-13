package dev.foraged.foxtrot.map.stats

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.IntegerPersistMap
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

@RegisterMap
@Listeners
object PotionsDrankPersistMap : IntegerPersistMap("PotionsDrank", "Potions_Drank", true), Listener
{
    @EventHandler
    fun onSplash(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.POTION) increment(event.player.uniqueId)
    }
}