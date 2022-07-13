package dev.foraged.foxtrot.command

import dev.foraged.commons.acf.annotation.CommandAlias
import dev.foraged.commons.acf.annotation.Default
import dev.foraged.commons.acf.annotation.Description
import dev.foraged.commons.annotations.commands.AutoRegister
import dev.foraged.commons.command.GoodCommand
import dev.foraged.foxtrot.server.ServerHandler
import dev.foraged.foxtrot.team.claim.LandBoard
import net.evilblock.cubed.util.CC
import org.bukkit.entity.Player

@CommandAlias("location|whereami|loc|here")
@AutoRegister
object LocationCommand : GoodCommand()
{
    @Default
    @Description("View your current location")
    fun execute(sender: Player) {
        val loc = sender.location
        val owner = LandBoard.getTeam(loc)

        if (owner != null) {
            sender.sendMessage("${CC.YELLOW}You are in ${owner.getName(sender.player)}${CC.YELLOW}'s territory.")
            return
        }

        if (!ServerHandler.isWarzone(loc)) sender.sendMessage("${CC.YELLOW}You are in ${CC.GRAY}The Wilderness${CC.YELLOW}.")
        else sender.sendMessage("${CC.YELLOW}You are in the ${CC.RED}Warzone${CC.YELLOW}.")
    }
}