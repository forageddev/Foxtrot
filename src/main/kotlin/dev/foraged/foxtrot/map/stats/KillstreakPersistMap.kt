package dev.foraged.foxtrot.map.stats

import dev.foraged.commons.annotations.Listeners
import dev.foraged.commons.persist.RegisterMap
import dev.foraged.commons.persist.impl.IntegerPersistMap
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

@RegisterMap
@Listeners
object KillstreakPersistMap : IntegerPersistMap("${Bukkit.getServerName()}Killstreak", "Killstreak", true, Bukkit.getServerName()), Listener
{
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        if (event.entity.killer != null) increment(event.entity.killer.uniqueId)
        reset(event.entity.uniqueId)
    }
}