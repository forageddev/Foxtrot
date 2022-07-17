package dev.foraged.foxtrot.listener

import dev.foraged.commons.annotations.Listeners
import dev.foraged.foxtrot.server.MapService
import net.evilblock.cubed.util.CC
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

@Listeners
object SignAdminListener : Listener
{
    @EventHandler
    fun onChange(event: SignChangeEvent) {
        if (MapService.isAdminOverride(event.player)) {
            event.lines.map { CC.translate(it) }.forEachIndexed { i, it ->
                event.setLine(i, it)
            }
        }
    }
}