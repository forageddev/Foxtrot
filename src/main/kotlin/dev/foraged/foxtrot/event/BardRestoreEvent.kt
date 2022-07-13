package dev.foraged.foxtrot.event

import dev.foraged.foxtrot.classes.PvPClass
import net.evilblock.cubed.event.PluginEvent
import org.bukkit.entity.Player

class BardRestoreEvent(val player: Player, val potion: PvPClass.SavedPotion) : PluginEvent()
