package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.metadata.FixedMetadataValue

@Listeners
object SneakListener : Listener
{
    @EventHandler
    fun onSneak(event: PlayerToggleSneakEvent) {
        if (event.isSneaking) {
            event.player.setMetadata("StartedSneaking", FixedMetadataValue(FoxtrotExtendedPlugin.instance, System.currentTimeMillis()))
        } else event.player.removeMetadata("StartedSneaking", FoxtrotExtendedPlugin.instance)
    }
}