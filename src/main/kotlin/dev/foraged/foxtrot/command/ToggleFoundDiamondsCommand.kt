package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.acf.annotation.Subcommand
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.FoundDiamondsPersistMap
import net.evilblock.cubed.util.CC
import net.evilblock.cubed.util.text.TextUtil
import org.bukkit.entity.Player

@AutoRegister
@CommandAlias("fd|fdtoggle|togglefd|togglefounddiamonds|tfd")
object ToggleFoundDiamondsCommand : GoodCommand()
{
    @Default
    @Subcommand("toggle")
    fun execute(sender: Player)
    {
        sender.sendMessage("${CC.SEC}Your diamond alerts are now ${CC.PRI}${
            TextUtil.stringifyBoolean(FoundDiamondsPersistMap.toggle(sender.uniqueId), TextUtil.FormatType.ENABLED_DISABLED).lowercase()
        }${CC.SEC}!")
    }
}