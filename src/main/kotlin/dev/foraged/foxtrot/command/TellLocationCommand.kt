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
@CommandAlias("telllocation|tl")
object TellLocationCommand : GoodCommand()
{
    @Default
    fun execute(sender: Player)
    {
        // todo: when team chat is implemented add tl
    }
}