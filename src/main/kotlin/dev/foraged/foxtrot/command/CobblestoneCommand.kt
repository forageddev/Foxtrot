package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.CobblestonePersistMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.entity.Player

@AutoRegister
@CommandAlias("cobblestone|cobble|stone")
object CobblestoneCommand : GoodCommand()
{
    @Default
    fun execute(sender: Player)
    {
        sender.sendMessage("${CC.SEC}Your cobblestone pickup is now ${
            TextUtil.stringifyBoolean(CobblestonePersistMap.toggle(sender.uniqueId), TextUtil.FormatType.ENABLED_DISABLED).lowercase()
        }${CC.SEC}!")
    }
}