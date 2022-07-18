package dev.foraged.foxtrot.map.stats

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.IntegerPersistMap
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PotionSplashEvent

@RegisterMap
@Listeners
object PotionsSplashedPersistMap : IntegerPersistMap("PotionsSplashed", "Potions_Splashed", true, Bukkit.getServerName()), Listener
{
    @EventHandler
    fun onSplash(event: PotionSplashEvent) {
        if (event.entity.shooter != null) increment((event.entity.shooter as Player).uniqueId)
    }
}