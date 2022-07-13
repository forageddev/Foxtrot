package dev.foraged.foxtrot.event

import net.evilblock.cubed.event.PluginEvent
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable

class CrowbarSpawnerBreakEvent(val player: Player, val block: Block) : PluginEvent(), Cancellable
{
    private var cancelled: Boolean = false

    override fun isCancelled(): Boolean { return cancelled }
    override fun setCancelled(cancel: Boolean) { cancelled = cancel }
}