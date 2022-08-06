package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.FoxtrotExtendedPlugin
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.metadata.FixedMetadataValue

@Listeners
object ArrowListener : Listener
{
    @EventHandler
    fun onShoot(event: EntityShootBowEvent) {
        if (event.entity is Player) event.projectile.setMetadata("ShotFromDistance", FixedMetadataValue(FoxtrotExtendedPlugin.instance, event.projectile.location))
    }
}