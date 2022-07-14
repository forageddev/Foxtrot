package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.ConditionFailedException
import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.map.cooldown.nopersist.LogoutMap
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

@AutoRegister
@CommandAlias("logout|log|getmeoutofhere")
object LogoutCommand : GoodCommand()
{
    @Default
    fun logout(sender: Player) {
        if (LogoutMap.isOnCooldown(sender.uniqueId)) throw ConditionFailedException("You are already logging out.")

        LogoutMap.startCooldown(sender.uniqueId)
        sender.sendMessage("${CC.B_YELLOW}LOGOUT: ${CC.RED}You have started your logout timer.")
        sender.sendMessage("${CC.RED}Be careful not to move or take damage.")
    }
}